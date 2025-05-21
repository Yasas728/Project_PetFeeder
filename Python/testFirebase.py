import firebase_admin
from firebase_admin import credentials
from firebase_admin import db

# Initialize Firebase app with your credentials
# You'll need to download a service account key JSON file from your Firebase project settings
cred = credentials.Certificate("D:\Chrome Downloads\petfeederdatabase-bd940-firebase-adminsdk-fbsvc-3ec6559ed1.json")
firebase_admin.initialize_app(cred, {
    'databaseURL': 'https://petfeederdatabase-bd940-default-rtdb.asia-southeast1.firebasedatabase.app/'
})

# Reference to your database
ref = db.reference('/')

# Get the variables
variables = ref.child('Variables').get()

# Print each variable
print("FeedNow:", variables.get('FeedNow'))
print("IntruderAlert:", variables.get('IntruderAlert'))
print("MainFoodLevel:", variables.get('MainFoodLevel'))
print("NextFeeding:", variables.get('NextFeeding'))
print("PotionSize:", variables.get('PotionSize'))

# Or access them individually if needed
feed_now = ref.child('Variables/FeedNow').get()
intruder_alert = ref.child('Variables/IntruderAlert').get()
main_food_level = ref.child('Variables/MainFoodLevel').get()
next_feeding = ref.child('Variables/NextFeeding').get()
potion_size = ref.child('Variables/PotionSize').get()

print("\nIndividual access:")
print(f"FeedNow: {feed_now}")
print(f"IntruderAlert: {intruder_alert}")
print(f"MainFoodLevel: {main_food_level}")
print(f"NextFeeding: {next_feeding}")
print(f"PotionSize: {potion_size}")
