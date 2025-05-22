import cv2
import numpy as np
import os
import time
from ultralytics import YOLO
from datetime import datetime

class PetDetector:
    def __init__(self):
        # Create output folders
        self.output_dir = "detected_pets"
        self.cats_dir = os.path.join(self.output_dir, "cats")
        self.dogs_dir = os.path.join(self.output_dir, "dogs")
        
        # Create directories if they don't exist
        os.makedirs(self.cats_dir, exist_ok=True)
        os.makedirs(self.dogs_dir, exist_ok=True)
        
        # Load YOLOv8 model
        print("Loading YOLO model...")
        self.model = YOLO("yolov8n.pt")  # Using the nano version for speed
        
        # Pet classes in COCO dataset (YOLOv8 default)
        self.cat_class_id = 15  # Cat class ID in COCO
        self.dog_class_id = 16  # Dog class ID in COCO
        
        # Load face detection model - using a simple Haar cascade for now
        # This is a lightweight approach; more accurate models available if needed
        self.cat_face_cascade = cv2.CascadeClassifier(cv2.data.haarcascades + 'haarcascade_frontalcatface.xml')
        
        # Since dog face detector isn't available in OpenCV, we'll use the detected body and adjust
        
        print("Models loaded successfully!")
        
    def detect_and_crop(self, frame):
        # Run YOLOv8 inference on the frame
        results = self.model(frame)
        
        # Process detection results
        for result in results:
            boxes = result.boxes
            for box in boxes:
                # Get box coordinates
                x1, y1, x2, y2 = map(int, box.xyxy[0])
                
                # Get class and confidence
                cls = int(box.cls[0])
                conf = float(box.conf[0])
                
                # Only process cats and dogs with confidence > 0.5
                if conf < 0.5:
                    continue
                
                # Initialize color and label with default values
                color = (255, 255, 0)  # Default color (cyan)
                label = "Unknown"
                    
                if cls == self.cat_class_id:  # It's a cat
                    label = "Cat"
                    color = (0, 255, 0)  # Green for cats
                    
                    # Crop the detected cat region
                    cat_img = frame[y1:y2, x1:x2].copy()
                    
                    # Try to detect cat face
                    gray = cv2.cvtColor(cat_img, cv2.COLOR_BGR2GRAY)
                    cat_faces = self.cat_face_cascade.detectMultiScale(gray, 1.1, 4)
                    
                    # If a face is found, crop and save it
                    if len(cat_faces) > 0:
                        for (fx, fy, fw, fh) in cat_faces:
                            # Add some margin
                            margin = int(max(fw, fh) * 0.2)
                            fx_with_margin = max(0, fx - margin)
                            fy_with_margin = max(0, fy - margin)
                            fw_with_margin = min(fw + 2 * margin, cat_img.shape[1] - fx_with_margin)
                            fh_with_margin = min(fh + 2 * margin, cat_img.shape[0] - fy_with_margin)
                            
                            # Crop face
                            face_crop = cat_img[fy_with_margin:fy_with_margin+fh_with_margin, 
                                            fx_with_margin:fx_with_margin+fw_with_margin]
                            
                            # Save cropped face
                            timestamp = datetime.now().strftime("%Y%m%d_%H%M%S_%f")
                            filename = os.path.join(self.cats_dir, f"cat_face_{timestamp}.jpg")
                            cv2.imwrite(filename, face_crop)
                            print(f"Saved cat face to {filename}")
                            
                            # Draw rectangle on original frame
                            cv2.rectangle(frame, 
                                        (x1 + fx_with_margin, y1 + fy_with_margin), 
                                        (x1 + fx_with_margin + fw_with_margin, y1 + fy_with_margin + fh_with_margin), 
                                        (255, 0, 255), 2)
                    else:
                        # If no face detected, just save the whole pet
                        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S_%f")
                        filename = os.path.join(self.cats_dir, f"cat_{timestamp}.jpg")
                        cv2.imwrite(filename, cat_img)
                        print(f"Saved cat image to {filename}")
                
                elif cls == self.dog_class_id:  # It's a dog
                    label = "Dog"
                    color = (0, 0, 255)  # Red for dogs
                    
                    # For dogs, estimate the face region (typically upper 1/3 of the body)
                    # This is a simple heuristic, not perfect
                    dog_img = frame[y1:y2, x1:x2].copy()
                    h, w = dog_img.shape[:2]
                    
                    # Estimate face location - typically in the upper portion of the detection
                    face_y1 = 0
                    face_y2 = int(h * 0.4)  # Upper 40% of the body
                    face_x1 = int(w * 0.2)  # Centered horizontally with some margin
                    face_x2 = int(w * 0.8)
                    
                    # Ensure we're within bounds
                    face_y2 = min(face_y2, h)
                    face_x2 = min(face_x2, w)
                    
                    if face_x2 > face_x1 and face_y2 > face_y1:
                        face_crop = dog_img[face_y1:face_y2, face_x1:face_x2]
                        
                        # Save cropped face
                        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S_%f")
                        filename = os.path.join(self.dogs_dir, f"dog_face_{timestamp}.jpg")
                        cv2.imwrite(filename, face_crop)
                        print(f"Saved estimated dog face to {filename}")
                        
                        # Draw rectangle on original frame
                        cv2.rectangle(frame, 
                                    (x1 + face_x1, y1 + face_y1), 
                                    (x1 + face_x2, y1 + face_y2), 
                                    (255, 0, 255), 2)
                else:
                    # Skip objects that are neither cats nor dogs
                    continue
                
                # Draw bounding box and label for the pet
                cv2.rectangle(frame, (x1, y1), (x2, y2), color, 2)
                cv2.putText(frame, f"{label} {conf:.2f}", (x1, y1 - 10), 
                        cv2.FONT_HERSHEY_SIMPLEX, 0.5, color, 2)
                
        return frame

    def start_webcam(self):
        # Open webcam
        cap = cv2.VideoCapture(0)
        
        if not cap.isOpened():
            print("Error: Could not open webcam.")
            return
        
        print("Webcam opened successfully. Press 'q' to quit.")
        
        # Initialize variables
        last_capture_time = 0
        interval = 5  # seconds
        frozen_frame = None
        is_detection_phase = True
        
        while True:
            current_time = time.time()
            
            # Time to switch phases (every 5 seconds)
            if current_time - last_capture_time >= interval:
                last_capture_time = current_time
                is_detection_phase = not is_detection_phase
                
                # If entering detection phase, capture a new frame
                if is_detection_phase:
                    ret, frame = cap.read()
                    if not ret:
                        print("Error: Failed to capture frame.")
                        break
                    
                    # Process and store the frame with detections
                    frozen_frame = self.detect_and_crop(frame)
                    print(f"Detection performed at {datetime.now().strftime('%H:%M:%S')}")
            
            # During display phase, just show the frozen frame
            # During detection phase, show "Processing..." text
            if is_detection_phase:
                # Show a frame with "Processing..." text
                ret, display_frame = cap.read()
                if not ret:
                    break
                    
                # Add text to indicate processing
                cv2.putText(display_frame, "Processing...", 
                        (10, 30), cv2.FONT_HERSHEY_SIMPLEX, 0.7, (0, 0, 255), 2)
                cv2.imshow('Pet Detector', display_frame)
            else:
                # Show the frozen detection frame
                if frozen_frame is not None:
                    # Add text showing seconds until next capture
                    time_left = int(interval - (current_time - last_capture_time))
                    cv2.putText(frozen_frame, f"Detected! Next capture in: {time_left}s", 
                            (10, 30), cv2.FONT_HERSHEY_SIMPLEX, 0.7, (0, 255, 0), 2)
                    cv2.imshow('Pet Detector', frozen_frame)
            
            # Exit on 'q' press
            if cv2.waitKey(1) & 0xFF == ord('q'):
                break
                
        # Release resources
        cap.release()
        cv2.destroyAllWindows()

if __name__ == "__main__":
    detector = PetDetector()
    detector.start_webcam()