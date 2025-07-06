import time
from hx711 import HX711  # From hx711py

# Create HX711 object with DT and SCK GPIO pins
hx = HX711(dout_pin=5, pd_sck_pin=6)

# Reset and calibrate
hx.set_reading_format("MSB", "MSB")
hx.set_reference_unit(1)  # Change this after calibration
hx.reset()
hx.tare()

print("Tare done. Ready to read weight...")

while True:
    try:
        weight = hx.get_weight(5)
        print("Weight: {:.2f} g".format(weight))
        hx.power_down()
        hx.power_up()
        time.sleep(1)
    except (KeyboardInterrupt, SystemExit):
        print("Cleaning up...")
        break
