package za.co.entelect.challenge;

import za.co.entelect.challenge.command.*;
import za.co.entelect.challenge.entities.*;
import za.co.entelect.challenge.enums.*;

import java.util.*;

import static java.lang.Math.max;

import java.security.SecureRandom;

public class Bot {

    private static final int maxSpeed = 9;
    private List<Command> directionList = new ArrayList<>();

    private final Random random;
    /*
     * private Random random;
     * private GameState gameState;
     */
    private Car opponent;
    private Car myCar;

    private final static Command ACCELERATE = new AccelerateCommand();
    private final static Command LIZARD = new LizardCommand();
    private final static Command OIL = new OilCommand();
    private final static Command BOOST = new BoostCommand();
    private final static Command EMP = new EmpCommand();
    private final static Command FIX = new FixCommand();

    private final static Command TURN_RIGHT = new ChangeLaneCommand(1);
    private final static Command TURN_LEFT = new ChangeLaneCommand(-1);

    public Bot(Random random, GameState gameState) {
        this.random = new SecureRandom();
        // this.gameState = gameState;
        // this.myCar = gameState.player;
        // this.opponent = gameState.opponent;
        directionList.add(TURN_LEFT);
        directionList.add(TURN_RIGHT);
    }

    public Command run(GameState gameState) {
        Car myCar = gameState.player;
        Car opponent = gameState.opponent;
        // List<Object> blocks =
        // getBlocksInFront(myCar.position.lane,myCar.position.block);
        if (myCar.damage >= 2) {
            return FIX;
        }
        if (hasPowerUp(PowerUps.BOOST, myCar.powerups) && myCar.damage == 0 && myCar.speed < 15) {
            projectedCar booster = perkiraan(myCar.position.lane, myCar.position.block, 15, myCar.damage, 0, gameState);

            if (booster.speed == 15) {
                return BOOST;
            }
        }
        projectedCar goForward = perkiraan(myCar.position.lane, myCar.position.block, nextSpeed(myCar.speed, myCar.damage),
                myCar.damage, 0, gameState);
        if (goForward.speed == nextSpeed(myCar.speed, myCar.damage)) {
            if (goForward.speed == myCar.speed) {
                // HARUSNYA DISINI NYERANG PEMAEN LAEN
                return ACCELERATE;
            } else {
                return ACCELERATE;
            }
        } else {
            projectedCar turnLeft = new projectedCar();
            projectedCar turnRight = new projectedCar();
            turnLeft =  perkiraan(myCar.position.lane - 1, myCar.position.block, myCar.speed - 1, myCar.damage, 1, gameState);
            turnRight = perkiraan(myCar.position.lane + 1, myCar.position.block, myCar.speed - 1, myCar.damage, 1, gameState);

            switch (bestMove(goForward, turnLeft, turnRight, myCar, gameState)) {
                case 1:
                    return ACCELERATE;
                case 2:
                    return TURN_LEFT;
                case 3:
                    return TURN_RIGHT;
                case 4:
                    return LIZARD;
                case 5:
                    // HARUSNYA DISINI NYERANG PEMAEN LAEN
                    if (hasPowerUp(PowerUps.OIL, myCar.powerups))  { return OIL; }
                    if (hasPowerUp(PowerUps.EMP, myCar.powerups))  { return EMP; }
                    return ACCELERATE;
                default:
                    break;
            }
        }
        return ACCELERATE;
    }

    /* CHECK KEBERADAAN SUATU POWER UP */
    private Boolean hasPowerUp(PowerUps powerUpToCheck, PowerUps[] powerUps) {
        for (PowerUps powerUp : powerUps) {
            if (powerUp.equals(powerUpToCheck)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns map of blocks and the objects in the for the current lanes, returns
     * the amount of blocks that can be
     * traversed at max speed.
     **/
    private List<Object> getBlocksInFront(int lane, int block, GameState gameState) {
        List<Lane[]> map = gameState.lanes;
        List<Object> blocks = new ArrayList<>();
        int startBlock = map.get(0)[0].position.block;

        Lane[] laneList = map.get(lane - 1);
        for (int i = max(block - startBlock, 0); i <= block - startBlock + Bot.maxSpeed; i++) {
            if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                break;
            }

            blocks.add(laneList[i].terrain);

        }
        return blocks;
    }

    /* ADDITIONAL SECTION */
    public class projectedCar {
        public int speed;
        public int damage;
    }

    /* 
    FUNGSI UTK RETURN SPEED STATE BERIKUTNYA
    DARI SPEED STATE SAAT INI (dengan mempertimbangkan kondisi damage)
    */
    int nextSpeed(int input_speed, int damage) {
        if (input_speed == 0 && damage < 5) { return 3; }
        if (input_speed == 3 && damage < 4) { return 6; }
        if (input_speed == 5 && damage < 4) { return 6; }
        if (input_speed == 6 && damage < 3) { return 8; }
        if (input_speed == 9 && damage < 2) { return 9; } // ini mksdnya input_speed == 8 ??
        
        return input_speed;
    }
    
    /* 
    FUNGSI UTK RETURN SPEED STATE SEBELUMNYA 
    DARI SPEED STATE SAAT INI (dengan mempertimbangkan kondisi damage)
    */
    int prevSpeed(int input_speed, int damage) { // ini return speed misal speedState turun | ini damagenya jg harus dipertimbangin gasih??
        if (damage >= 5)        { return 0; }
        if (input_speed == 3)   { return 3; }  // ini maksudnya return 0 ???
        if (input_speed == 15)  { return 9; }
        if (input_speed == 9)   { return 8; }
        if (input_speed == 8)   { return 6; }
        if (input_speed == 6)   { return 3; }
        if (input_speed == 5)   { return 3; }

        return input_speed;
    }

    /* ??? */
    private projectedCar perkiraan(int lane, int block, int speed, int damage, int kirikanan, GameState gameState) {
        projectedCar output = new projectedCar();
        if(lane < 1 || lane > 4){
            output.speed = -1;
            output.damage = 10;
            return output;
        }
        List<Lane[]> map = gameState.lanes;
        // List<Object> blocks = new ArrayList<>();
        int startBlock = map.get(0)[0].position.block;
        output.speed = speed + kirikanan;
        output.damage = damage;
        Lane[] laneList = map.get(lane - 1);

        // ini bisa langsung i = block - startBlock, ga sih?
        // dan bisa block-startBlock = block gasih??
        for (int i = max(block - startBlock, 0); i <= block - startBlock + speed; i++) {
            if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                break;
            }
            if (laneList[i].terrain == Terrain.MUD) {
                output.damage += 1;
                output.speed = prevSpeed(output.speed, output.damage);
            }
            if (laneList[i].terrain == Terrain.OIL_SPILL) {
                output.damage += 1;
                output.speed = prevSpeed(output.speed, output.damage);
            }
            if (laneList[i].terrain == Terrain.WALL) {
                output.damage += 2;
                output.speed = 3;
                output.speed = prevSpeed(output.speed, output.damage); // ini buat apa lagi?
            }
            if(laneList[i].isOccupiedByCyberTruck){
                output.damage += 2;
                output.speed = 3;
                output.speed = prevSpeed(output.speed, output.damage); // ini buat apa lagi?
            }
        }
        return output;
    }

    // ini misal di lane 1 atau 4 gimana?
    private int bestMove(projectedCar goForward, projectedCar turnLeft, projectedCar turnRight, Car myCar, GameState gameState) {
        // 1 - Maju, 2 - Kiri, 3- Kanan, 4 - LIZARD, 5 - Nyerang Orang
        if(turnLeft.speed < myCar.speed && turnRight.speed < myCar.speed){
            int lizard = isWorth_useLizard(myCar.position.lane, myCar.position.block, myCar.speed, gameState);
            if (lizard > goForward.speed){
                return 4;
            }
        }
        if(goForward.speed == turnLeft.speed && goForward.speed == turnLeft.speed){ // mungkin ini mksdny goForward.speed == turnRight.speed
            if(goForward.damage == turnLeft.damage && goForward.damage == turnRight.damage){
                return 5;
            }
        }
        if(goForward.speed >= turnRight.speed && goForward.speed >= turnLeft.speed){
            if(goForward.damage <= turnLeft.damage && goForward.damage <= turnRight.damage) {
                return 1;
            }
        }
        if(myCar.position.lane == 3){
            if(turnLeft.speed >= goForward.speed && turnLeft.speed >= turnRight.speed) {
                if (turnLeft.damage <= goForward.damage && turnLeft.damage <= turnRight.damage) {
                    return 2;
                }
            }
            return 3;
        }
        else {
            if(turnRight.speed >= goForward.speed && turnRight.speed >= turnLeft.speed){
                if(turnRight.damage <= goForward.damage && turnRight.damage <= turnLeft.damage){
                    return 3;
                }
            }
            return 2;
        }
    }

    /* 
    FUNGSI MENENTUKAN APAKAH POWERUP LIZARD WORTH UNTUK DIPAKAI 
    */
    int isWorth_useLizard(int lane, int block, int speed, GameState gameState) {
        List<Lane[]> map = gameState.lanes;
        // List<Object> blocks = new ArrayList<>();
        int startBlock = map.get(0)[0].position.block; // ini garis start??
        boolean found = false;
        Lane[] laneList = map.get(lane - 1);    // ini maksudnya kalo di lane 2, brarti sama aja map.get(3 - 1) ??
        for (int i = max(block - startBlock, 0); i <= block - startBlock + speed; i++) {
            if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                found = true;
                break;
            }
            if (laneList[i].terrain == Terrain.MUD) {
                found = true;
                break;
            }
            if (laneList[i].terrain == Terrain.WALL) {
                found = true;
                break;
            }
        }
        if (!found) {
            return 0;
        } else {
            // ini kondisi pasti ga terpenuhi gasih??
            if (laneList[block - startBlock + speed].terrain != Terrain.MUD
                    && laneList[block - startBlock + speed].terrain != Terrain.WALL) {
                return 0;
            } else {
                return speed;
            }
        }
    }
}