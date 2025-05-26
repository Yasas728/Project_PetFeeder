from getSchedule import FeederSchedule
import schedule
from datetime import datetime, timedelta
import time
import requests


class UpdateFeedNow:

    
    def __init__(self, feeder=None):
        # Create feeder instance if not provided
        if feeder is None:
            self.feeder = FeederSchedule()
        else:
            self.feeder = feeder
            
        # Get initial next feeding time
        next_feeding = self.feeder.get_next_feeding_time()
        print(next_feeding)

        if next_feeding:
            print(f"Next feeding at {next_feeding['time']} on {next_feeding['day']}")
            print(f"In: {FeederSchedule.format_time_until(next_feeding['time_until'])}")
        else:
            print("No upcoming feeding scheduled.")
            
        # Start the scheduling loop
        self.run_scheduler()
    
    def run_scheduler(self):
        """Main scheduling loop"""
        while True:
            # Clear previous schedules to avoid duplicates
            schedule.clear()
            
            # Get current next feeding time
            next_feeding = self.feeder.get_next_feeding_time()
            
            if next_feeding:
                # Schedule the feeding
                schedule.every().day.at(next_feeding['time']).do(self.firebase_upload)
                self.nextFeedingTime_update(next_feeding)
                print(f"Scheduled feeding for {next_feeding['time']} on {next_feeding['day']}")
            
            # Run pending schedules
            schedule.run_pending()
            time.sleep(30)
    
    @staticmethod
    def firebase_upload():
        """Upload feed command to Firebase"""
        url = "https://petfeederdatabase-bd940-default-rtdb.asia-southeast1.firebasedatabase.app/Variables.json"
        data = {"FeedNow": True}
        
        try:
            response = requests.patch(url, json=data)
            
            # Check result
            if response.status_code == 200:
                print("Feed command sent successfully!")
                print(f"Response: {response.json()}")
            else:
                print(f"Failed to send feed command. Status code: {response.status_code}")
                print(f"Response: {response.text}")
                
        except requests.exceptions.RequestException as e:
            print(f"Network error occurred: {e}")
        except Exception as e:
            print(f"Unexpected error: {e}")

            
    @staticmethod
    def nextFeedingTime_update(next_feeding):
        url = "https://petfeederdatabase-bd940-default-rtdb.asia-southeast1.firebasedatabase.app/Variables.json"

        today = datetime.today().strftime('%a')
        tomorrow = datetime.today() + timedelta(days=1)
        day_tomorrow = tomorrow.strftime('%a')

        time_12 = datetime.strptime(next_feeding['time'], "%H:%M").strftime("%I:%M %p")


        if today == next_feeding['day']:
            day_name = "Today"
        elif day_tomorrow == next_feeding['day']:
            day_name = "Tomorrow"
        else:
            day_name = next_feeding['day']
        
        
        text = {"NextFeeding": f"{day_name} at {time_12}"}
        try:
            response = requests.patch(url, json=text)
            
            # Check result
            if response.status_code == 200:
                print("Next Feeding time updated successfully!")
                print(f"Response: {response.json()}")
            else:
                print(f"Failed to send feed command. Status code: {response.status_code}")
                print(f"Response: {response.text}")
                
        except requests.exceptions.RequestException as e:
            print(f"Network error occurred: {e}")
        except Exception as e:
            print(f"Unexpected error: {e}")

            
# Create and run the feeder updater
if __name__ == "__main__":
    try:
        updater = UpdateFeedNow()
    except KeyboardInterrupt:
        print("\nFeeder scheduler stopped by user.")
    except Exception as e:
        print(f"Error starting feeder scheduler: {e}")
