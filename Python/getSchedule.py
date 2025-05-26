import requests
from datetime import datetime, timedelta

class FeederSchedule:
    def __init__(self):
        self.url = "https://petfeederdatabase-bd940-default-rtdb.asia-southeast1.firebasedatabase.app/Schedules.json"
        self.schedule_data = None
        self.variables = None

    def get_schedule_data(self):
        try:
            response = requests.get(self.url)
            if response.status_code == 200:
                self.schedule_data = response.json()
                return self.schedule_data
            else:
                print(f"Error: {response.status_code}")
                return None
        except Exception as e:
            print(f"Error retrieving data: {e}")
            return None

    def extract_schedule_variables(self):
        if not self.schedule_data:
            return None

        variables = {}

        if isinstance(self.schedule_data, list):
            for i, schedule_info in enumerate(self.schedule_data):
                if schedule_info is not None:
                    variables[i] = self._extract_fields(schedule_info)
        else:
            for schedule_id, schedule_info in self.schedule_data.items():
                variables[schedule_id] = self._extract_fields(schedule_info)

        self.variables = variables
        return variables

    def _extract_fields(self, schedule_info):
        return {
            'enable': schedule_info.get('enable', False),
            'fri': schedule_info.get('fri', False),
            'id': schedule_info.get('id', 0),
            'mon': schedule_info.get('mon', False),
            'sat': schedule_info.get('sat', False),
            'sun': schedule_info.get('sun', False),
            'thu': schedule_info.get('thu', False),
            'timeHour': schedule_info.get('timeHour', 0),
            'timeMinute': schedule_info.get('timeMinute', 0),
            'tue': schedule_info.get('tue', False),
            'wed': schedule_info.get('wed', False)
        }

    def get_next_schedule_time(self):
        if not self.variables:
            return None

        now = datetime.now()
        day_mapping = {
            0: 'mon', 1: 'tue', 2: 'wed', 3: 'thu',
            4: 'fri', 5: 'sat', 6: 'sun'
        }

        next_schedules = []

        for schedule_id, schedule in self.variables.items():
            if not schedule['enable']:
                continue

            for day_offset in range(7):
                check_date = now + timedelta(days=day_offset)
                weekday_key = day_mapping[check_date.weekday()]

                if schedule[weekday_key]:
                    schedule_datetime = check_date.replace(
                        hour=schedule['timeHour'],
                        minute=schedule['timeMinute'],
                        second=0,
                        microsecond=0
                    )
                    if schedule_datetime > now:
                        next_schedules.append({
                            'schedule_id': schedule_id,
                            'datetime': schedule_datetime,
                            'day': weekday_key.capitalize(),
                            'time': f"{schedule['timeHour']:02d}:{schedule['timeMinute']:02d}",
                            'time_until': schedule_datetime - now
                        })

        next_schedules.sort(key=lambda x: x['datetime'])
        return next_schedules

    def get_next_feeding_time(self):
        self.get_schedule_data()
        self.extract_schedule_variables()
        schedules = self.get_next_schedule_time()
        if schedules:
            return schedules[0]
        return None

    @staticmethod
    def format_time_until(time_delta):
        total_seconds = int(time_delta.total_seconds())
        days = total_seconds // 86400
        hours = (total_seconds % 86400) // 3600
        minutes = (total_seconds % 3600) // 60

        if days > 0:
            return f"{days} day(s), {hours} hour(s), {minutes} minute(s)"
        elif hours > 0:
            return f"{hours} hour(s), {minutes} minute(s)"
        else:
            return f"{minutes} minute(s)"
