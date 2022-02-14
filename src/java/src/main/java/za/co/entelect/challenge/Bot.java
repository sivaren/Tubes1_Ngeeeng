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
    private Car opponent;
    private Car myCar;

    private final static Command ACCELERATE = new AccelerateCommand();
    private final static Command TURN_LEFT = new ChangeLaneCommand(-1);
    private final static Command TURN_RIGHT = new ChangeLaneCommand(1);
    private final static Command USE_BOOST = new BoostCommand();
    private final static Command USE_OIL = new OilCommand();
    private final static Command USE_LIZARD = new LizardCommand();
    private final static Command USE_EMP = new EmpCommand();
    private final static Command FIX = new FixCommand();

    public Bot(Random random, GameState gameState) {
        this.random = new SecureRandom();
        directionList.add(TURN_LEFT);
        directionList.add(TURN_RIGHT);
    }

    public Command run(GameState gameState) {
        Car myCar = gameState.player;
        Car opponent = gameState.opponent;
        
        /* ALGORITMA */

        /* Skenario 1: mobil memiliki damage >= 2 */
        // Command FIX akan digunakan
        // Karena satu kali menggunakan command FIX akan menurunkan damage sebanyak 2,
        // command FIX hanya digunakan bila damage >= 2
        if (myCar.damage >= 2) {
            return FIX;
        }

        /* Skenario 2: mobil memiliki damage < 2 dan memiliki BOOST */
        // Command USE_BOOST akan digunakan
        // Command USE_BOOST akan digunakan bila speed mobil setelah di-boost menjadi 15,
        // yaitu ketika tidak ada halangan yang dapat mengurangi speed
        if (hasPowerUp(PowerUps.BOOST, myCar.powerups) && myCar.damage == 0 && myCar.speed < 15) {
            projectedCar booster = perkiraan(myCar.position.lane, myCar.position.block, 15, myCar.damage, 0, gameState);
            if (booster.speed == 15) {
                return USE_BOOST;
            }
        }

        /* Skenario 3: mobil memiliki damage < 2, namun tidak memiliki BOOST */
        // Command yang akan dipakai: ACCELERATE, TURN_RIGHT, TURN_LEFT, USE_LIZARD, atau menyerang lawan
        // Memperkirakan kondisi bila mobil maju (lurus)
        projectedCar goForward = perkiraan(myCar.position.lane, myCar.position.block, nextSpeed(myCar.speed, myCar.damage),
                myCar.damage, 0, gameState);

        // Bila tidak ada halangan yang akan menurunkan speed mobil, mobil akan jalan lurus
        // dengan dua opsi: sambil menyerang pemain lain atau sambil akselerasi
        if (goForward.speed == nextSpeed(myCar.speed, myCar.damage)) {
            if (goForward.speed == myCar.speed) {
                // Bila akselerasi tidak menaikkan speed mobil, serangan pada lawan akan diluncurkan
                return attackConsideration(myCar, opponent);
            } else {
                // Bila akselerasi masih dapat menaikkan speed mobil, maka mobil berakselerasi
                return ACCELERATE;
            }

        // Bila ada halangan yang akan menurunkan speed mobil,
        // algoritma akan membandingkan keuntungan ACCELERATE, TURN_LEFT, TURN_RIGHT, USE_LIZARD, atau menyerang lawan
        } else {
            projectedCar turnLeft = new projectedCar();
            projectedCar turnRight = new projectedCar();
            turnLeft = perkiraan(myCar.position.lane - 1, myCar.position.block, myCar.speed - 1, myCar.damage, 1, gameState);
            turnRight = perkiraan(myCar.position.lane + 1, myCar.position.block, myCar.speed - 1, myCar.damage, 1, gameState);

            switch (bestMove(goForward, turnLeft, turnRight, myCar, gameState)) {
                case 1:
                    return ACCELERATE;
                case 2:
                    return TURN_LEFT;
                case 3:
                    return TURN_RIGHT;
                case 4:
                    return USE_LIZARD;
                case 5:
                    return attackConsideration(myCar, opponent);
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
    /* CHECK KEBERADAAN SUATU POWER UP */

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
    /* GETTER */
    private int getOpponentLane (Car opponent)  { return opponent.position.lane;    }
    private int getOpponentBlock (Car opponent) { return opponent.position.block;   }
    private int getMyCarLane (Car myCar)        { return myCar.position.lane;       }
    private int getMyCarBlock (Car myCar)       { return myCar.position.block;      }
    /* GETTER */

    /* FUNGSI UTK MENGECEK APAKAH POSISI MUSUH BERADA DI BELAKANG MYCAR */
    private Boolean isOpponentBehind (Car myCar, Car opponent) { 
        int myCarBlock = getMyCarBlock (myCar);
        int opponentBlock = getOpponentBlock (opponent);

        if (opponentBlock < myCarBlock) {
            return true;
        } else {
            return false;
        }
    }
    /* FUNGSI UTK MENGECEK APAKAH POSISI MUSUH BERADA DI BELAKANG MYCAR */

    /* TWEET - OIL - EMP */
    private Command attackConsideration (Car myCar, Car opponent) {
        if (isOpponentBehind (myCar, opponent)) {
            if (hasPowerUp(PowerUps.TWEET, myCar.powerups)) {
                int opponentLane = getOpponentLane (opponent); 
                int opponentBlock = getOpponentBlock (opponent); 
                Command USE_TWEET = new TweetCommand(opponentLane, opponentBlock + 1);
                
                return USE_TWEET;
            }
            if (hasPowerUp(PowerUps.OIL, myCar.powerups)) { return USE_OIL; }
            if (hasPowerUp(PowerUps.EMP, myCar.powerups)) { return USE_EMP; }
        } else {
            int opponentLane = getOpponentLane (opponent);
            int opponentBlock = getOpponentBlock (opponent);
            int myCarLane = getMyCarLane (myCar);
            if (myCarLane-opponentLane <= 1 && myCarLane-opponentLane >= -1) {
                if (hasPowerUp(PowerUps.EMP, myCar.powerups)) { return USE_EMP; }
            }
            if (hasPowerUp(PowerUps.TWEET, myCar.powerups)) {
                Command USE_TWEET = new TweetCommand(opponentLane, opponentBlock + 1);
                return USE_TWEET;
            }
            if (hasPowerUp(PowerUps.OIL, myCar.powerups)) { return USE_OIL; }
        }
        return ACCELERATE;
    }
    /* TWEET - OIL - EMP */

    /* CLASS projectedCar */
    public class projectedCar {
        public int speed;
        public int damage;
    }
    /* CLASS projectedCar */

    /* 
    * FUNGSI UTK RETURN SPEED STATE BERIKUTNYA
    * DARI SPEED STATE SAAT INI (dengan mempertimbangkan kondisi damage)
    */
    private int nextSpeed(int input_speed, int damage) {
        if (input_speed == 0 && damage < 5) { return 3; }
        if (input_speed == 3 && damage < 4) { return 6; }
        if (input_speed == 5 && damage < 4) { return 6; }
        if (input_speed == 6 && damage < 3) { return 8; }
        if (input_speed == 8 && damage < 2) { return 9; }
        if (input_speed == 9 && damage < 1) { return 9; }
        return input_speed;
    }
    
    /* 
    * FUNGSI UTK RETURN SPEED STATE SEBELUMNYA 
    * DARI SPEED STATE SAAT INI (dengan mempertimbangkan kondisi damage)
    */
    private int prevSpeed(int input_speed, int damage) { 
        if (damage >= 5)        { return 0; }
        if (input_speed == 3)   { return 0; }
        if (input_speed == 15)  { return 9; }
        if (input_speed == 9)   { return 8; }
        if (input_speed == 8)   { return 6; }
        if (input_speed == 6)   { return 3; }
        if (input_speed == 5)   { return 3; }

        return input_speed;
    }

    private projectedCar perkiraan(int lane, int block, int speed, int damage, int kirikanan, GameState gameState) {
        projectedCar output = new projectedCar();
        if(lane < 1 || lane > 4){
            output.speed = -1;
            output.damage = 10;
            return output;
        }
        List<Lane[]> map = gameState.lanes;
        int startBlock = map.get(0)[0].position.block;
        output.speed = speed + kirikanan;
        output.damage = damage;
        Lane[] laneList = map.get(lane - 1);

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
                output.speed = prevSpeed(output.speed, output.damage); // mempertimbangkan damage
            }
            if(laneList[i].isOccupiedByCyberTruck){
                output.damage += 2;
                output.speed = 3;
                output.speed = prevSpeed(output.speed, output.damage); // mempertimbangkan damage
            }
        }
        return output;
    }

    private int bestMove(projectedCar goForward, projectedCar turnLeft, projectedCar turnRight, Car myCar, GameState gameState) {
        // 1 - Maju, 2 - Kiri, 3- Kanan, 4 - LIZARD, 5 - Nyerang Orang
        if(turnLeft.speed < myCar.speed && turnRight.speed < myCar.speed){
            int lizard = isWorth_useLizard(myCar.position.lane, myCar.position.block, myCar.speed, gameState);
            if (lizard > goForward.speed){
                return 4;
            }
        }
        if(goForward.speed == turnLeft.speed && goForward.speed == turnRight.speed){
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

    /* FUNGSI MENENTUKAN APAKAH POWERUP LIZARD WORTH UNTUK DIPAKAI */
    int isWorth_useLizard(int lane, int block, int speed, GameState gameState) {
        List<Lane[]> map = gameState.lanes;
        int startBlock = map.get(0)[0].position.block; 
        boolean found = false;
        Lane[] laneList = map.get(lane - 1);   
        for (int i = max(block - startBlock, 0); i <= block - startBlock + speed; i++) {
            if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                return speed;
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
            if (laneList[block - startBlock + speed].terrain == Terrain.MUD
                    || laneList[block - startBlock + speed].terrain == Terrain.WALL
                    || laneList[block - startBlock + speed].terrain == Terrain.OIL_SPILL
                    || laneList[block - startBlock + speed].isOccupiedByCyberTruck) {
                return 0;
            } else {
                return speed;
            }
        }
    }
}