import requests
from datetime import datetime, timedelta
import pytz
import time
from pprint import pprint


class PetFeeder:
    def __init__(self):
        """Initialize the pet feeder with timezone and Firebase URL"""
        self.tz = pytz.timezone("Asia/Colombo")
        self.base_url = "https://petfeederdatabase-bd940-default-rtdb.asia-southeast1.firebasedatabase.app"
        self.last_feed_time = None
        self.startup_test()

    def startup_test(self):
        """Verify all systems are operational before starting"""
        print("\n" + "=" * 50)
        print(f"🚀 Starting Pet Feeder at {datetime.now(self.tz).strftime('%Y-%m-%d %H:%M:%S')}")
        print("=" * 50)

        if not self._test_firebase_connection():
            raise ConnectionError("Failed to connect to Firebase")

        print("\n✅ System checks passed. Starting scheduler...")

    def _test_firebase_connection(self):
        """Test Firebase read/write capabilities"""
        print("\n🔌 Testing Firebase connection...")
        try:
            # Test read access
            status_url = f"{self.base_url}/.json"
            response = requests.get(status_url, timeout=5)
            print(f"📡 Database read: {'✅' if response.status_code == 200 else '❌'} ({response.status_code})")

            # Test write access
            test_data = {"ConnectionTest": datetime.now(self.tz).isoformat()}
            response = requests.patch(status_url, json=test_data, timeout=5)
            print(f"✏️ Database write: {'✅' if response.status_code == 200 else '❌'} ({response.status_code})")

            return response.status_code == 200
        except Exception as e:
            print(f"❌ Connection failed: {str(e)}")
            return False

    def run(self):
        """Main scheduling loop"""
        print("\n🔄 Starting scheduler...")

        while True:
            try:
                now = datetime.now(self.tz)
                next_feeding = self._get_next_feeding_time()

                if next_feeding:
                    self._update_display(next_feeding)
                    self._check_feeding_time(next_feeding, now)

                time.sleep(5)  # Check every 5 seconds

            except Exception as e:
                print(f"⚠️ Scheduler error: {str(e)}")
                time.sleep(10)  # Wait longer after errors

    def _get_next_feeding_time(self):
        """Calculate the next scheduled feeding time"""
        try:
            response = requests.get(f"{self.base_url}/Schedules.json", timeout=5)
            if response.status_code != 200:
                return None

            schedules = response.json()
            now = datetime.now(self.tz)
            next_feeding = None

            for schedule_id, schedule in schedules.items():
                if not schedule.get('enable', False):
                    continue

                for day_offset in range(7):  # Check next 7 days
                    check_date = now + timedelta(days=day_offset)
                    weekday = check_date.strftime('%a').lower()

                    if schedule.get(weekday, False):
                        feed_time = check_date.replace(
                            hour=schedule['timeHour'],
                            minute=schedule['timeMinute'],
                            second=0,
                            microsecond=0
                        )

                        if feed_time > now and (next_feeding is None or feed_time < next_feeding['datetime']):
                            next_feeding = {
                                'datetime': feed_time,
                                'time': f"{schedule['timeHour']:02d}:{schedule['timeMinute']:02d}",
                                'day': weekday.capitalize()
                            }

            return next_feeding

        except Exception as e:
            print(f"⚠️ Schedule loading error: {str(e)}")
            return None

    def _check_feeding_time(self, next_feeding, current_time):
        """Check if it's time to trigger feeding"""
        time_until = (next_feeding['datetime'] - current_time).total_seconds()

        if 0 <= time_until <= 30:  # 30-second window
            if self.last_feed_time != next_feeding['datetime']:
                print(f"\n⏰ Feeding time detected: {next_feeding['time']}")
                self._trigger_feeding()
                self.last_feed_time = next_feeding['datetime']
            else:
                print(f"⏳ Already fed at {next_feeding['time']}")
        else:
            mins_until = int(time_until // 60)
            if mins_until > 0:
                print(f"\n⏳ Next feeding: {next_feeding['day']} at {next_feeding['time']} (in {mins_until} mins)")
            else:
                print(f"\n⏳ Next feeding: {next_feeding['day']} at {next_feeding['time']} (in {int(time_until)} secs)")

    def _trigger_feeding(self):
        """Send feed command to Firebase"""
        print("\n🎯 Triggering feeding sequence...")
        url = f"{self.base_url}/FeedCommand.json"

        try:
            # Verify current state
            current_state = requests.get(url, timeout=3).json()
            print(f"🔍 Pre-feed status: FeedNow={current_state.get('FeedNow')}")

            # Send feed command
            response = requests.patch(url, json={"FeedNow": True}, timeout=5)
            print(f"📡 Command sent: {'✅' if response.status_code == 200 else '❌'} ({response.status_code})")

            # Verify update
            updated_state = requests.get(url, timeout=3).json()
            print(f"✅ Post-feed status: FeedNow={updated_state.get('FeedNow')}")
            
            self.buzzerControl()

            return response.status_code == 200

        except Exception as e:
            print(f"❌ Feeding command failed: {str(e)}")
            return False

    def _update_display(self, next_feeding):
        """Update the NextFeeding display in Firebase"""
        now = datetime.now(self.tz)
        time_str = datetime.strptime(next_feeding['time'], "%H:%M").strftime("%I:%M %p")

        if now.strftime('%a') == next_feeding['day']:
            display_text = f"Today at {time_str}"
        elif (now + timedelta(days=1)).strftime('%a') == next_feeding['day']:
            display_text = f"Tomorrow at {time_str}"
        else:
            display_text = f"{next_feeding['day']} at {time_str}"

        try:
            response = requests.patch(
                f"{self.base_url}/Variables.json",
                json={"NextFeeding": display_text},
                timeout=3
            )
            if response.status_code != 200:
                print(f"⚠️ Display update failed (Status: {response.status_code})")
        except Exception as e:
            print(f"⚠️ Display update error: {str(e)}")

    def buzzerControl(self):
        time.sleep(30)
        url = f"{self.base_url}/Buzzer.json"

        response = requests.patch(url, json={"Enable": True}, timeout=5)
        print(f"Buzzer TRUE sent: {'✅' if response.status_code == 200 else '❌'} ({response.status_code})")
        
        time.sleep(30)

        response = requests.patch(url, json={"Enable": False}, timeout=5)
        print(f"Buzzer FALSE sent: {'✅' if response.status_code == 200 else '❌'} ({response.status_code})")
        
        
if __name__ == "__main__":
    print("🐾 Smart Pet Feeder Controller 🐾")
    try:
        feeder = PetFeeder()
        feeder.run()
    except KeyboardInterrupt:
        print("\n🛑 Scheduler stopped by user")
    except Exception as e:
        print(f"💥 Fatal error: {str(e)}")
