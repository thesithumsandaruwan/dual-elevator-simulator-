import java.util.*;

public class Lift {
    private int currentFloor;
    private Direction direction;
    private boolean[] floorButtons;
    private boolean[] upRequests;
    private boolean[] downRequests;
    private Timer timer;
    private boolean doorOpen;
    private long doorOpenTime;
    private static final long DOOR_OPEN_DURATION = 10000; // 10 seconds in milliseconds
    private long lastMoveTime;
    private static final long MOVE_DELAY = 2000; // 2 seconds between floors
    
    public Lift(int totalFloors) {
        this.currentFloor = 1;
        this.direction = Direction.IDLE;
        this.floorButtons = new boolean[totalFloors + 1];
        this.upRequests = new boolean[totalFloors + 1];
        this.downRequests = new boolean[totalFloors + 1];
        this.doorOpen = false;
        this.doorOpenTime = 0;
        this.lastMoveTime = System.currentTimeMillis();
    }

    public void requestFloor(int floor) {
        floorButtons[floor] = true;
        updateDirection();
    }

    public void externalRequest(int floor, Direction dir) {
        if (dir == Direction.UP) {
            upRequests[floor] = true;
        } else {
            downRequests[floor] = true;
        }
        updateDirection();
    }

    private void updateDirection() {
        // Logic to determine direction based on requests
        if (direction == Direction.IDLE) {
            // Check for any pending requests
            for (int i = currentFloor + 1; i < floorButtons.length; i++) {
                if (floorButtons[i] || upRequests[i] || downRequests[i]) {
                    direction = Direction.UP;
                    return;
                }
            }
            for (int i = currentFloor - 1; i > 0; i--) {
                if (floorButtons[i] || upRequests[i] || downRequests[i]) {
                    direction = Direction.DOWN;
                    return;
                }
            }
        }
    }
    
    public void move() {
        if (doorOpen) {
            if (System.currentTimeMillis() - doorOpenTime > DOOR_OPEN_DURATION) {
                doorOpen = false;
            }
            return;
        }
        
        // Check if enough time has passed for movement
        if (System.currentTimeMillis() - lastMoveTime < MOVE_DELAY) {
            return;
        }
        
        if (shouldStopAtCurrentFloor()) {
            handleStop();
            return;
        }
        
        if (direction == Direction.UP && hasUpwardDestination()) {
            currentFloor++;
            lastMoveTime = System.currentTimeMillis();
        } else if (direction == Direction.DOWN && hasDownwardDestination()) {
            currentFloor--;
            lastMoveTime = System.currentTimeMillis();
        } else {
            direction = Direction.IDLE;
        }
    }
    
    private boolean shouldStopAtCurrentFloor() {
        return floorButtons[currentFloor] || 
               (direction == Direction.UP && upRequests[currentFloor]) ||
               (direction == Direction.DOWN && downRequests[currentFloor]);
    }
    
    private void handleStop() {
        floorButtons[currentFloor] = false;
        if (direction == Direction.UP) {
            upRequests[currentFloor] = false;
        } else {
            downRequests[currentFloor] = false;
        }
        doorOpen = true;
        doorOpenTime = System.currentTimeMillis();
    }
    
    private boolean hasUpwardDestination() {
        for (int i = currentFloor + 1; i < floorButtons.length; i++) {
            if (floorButtons[i] || upRequests[i] || downRequests[i]) return true;
        }
        return false;
    }
    
    private boolean hasDownwardDestination() {
        for (int i = currentFloor - 1; i >= 1; i--) {
            if (floorButtons[i] || upRequests[i] || downRequests[i]) return true;
        }
        return false;
    }
    
    // Getters
    public int getCurrentFloor() { return currentFloor; }
    public boolean isDoorOpen() { return doorOpen; }
    public Direction getDirection() { return direction; }
}
