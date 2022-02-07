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
            projectedCar booster = perkiraan(myCar.position.lane, myCar.position.block, myCar.speed, myCar.damage,
                    gameState);
            if (booster.speed == 15) {
                return BOOST;
            }
        }
        projectedCar maju = perkiraan(myCar.position.lane, myCar.position.block, nextSpeed(myCar.speed, myCar.damage),
                myCar.damage,
                gameState);
        if (maju.speed == nextSpeed(myCar.speed, myCar.damage)) {
            if (maju.speed == myCar.speed) {
                // HARUSNYA DISINI NYERANG PEMAEN LAEN
                return ACCELERATE;
            } else {
                return ACCELERATE;
            }
        } else {
            projectedCar kiri = new projectedCar();
            projectedCar kanan = new projectedCar();
            if (myCar.position.lane - 1 > 0) {
                kiri = perkiraan(myCar.position.lane - 1, myCar.position.block, myCar.speed, myCar.damage, gameState);
            } else {
                kiri.speed = -1;
                kiri.damage = 10;
            }
            if (myCar.position.lane + 1 < 4) {
                kanan = perkiraan(myCar.position.lane + 1, myCar.position.block, myCar.speed, myCar.damage, gameState);
            } else {
                kanan.speed = -1;
                kanan.damage = 10;
            }
            switch (bestMove(maju, kiri, kanan, myCar, gameState)) {
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
                    return ACCELERATE;
                default:
                    break;
            }
        }
        return ACCELERATE;
    }

    // Check Available PowerUps
    private Boolean hasPowerUp(PowerUps powerUpToCheck, PowerUps[] available) {
        for (PowerUps powerUp : available) {
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

    // Additional Functions
    // =====================================================================
    public class projectedCar {
        public int speed;
        public int damage;
    }

    int nextSpeed(int input_speed, int damage) {
        if (input_speed == 0 && damage < 5) {
            return 3;
        }
        if (input_speed == 3 && damage < 4) {
            return 6;
        }
        if (input_speed == 5 && damage < 4) {
            return 6;
        }
        if (input_speed == 6 && damage < 3) {
            return 8;
        }
        if (input_speed == 9 && damage < 2) {
            return 9;
        }
        return input_speed;
    }

    int prevSpeed(int input_speed, int damage) {
        if (damage >= 5) {
            return 0;
        }
        if (input_speed == 3) {
            return 3;
        }
        if (input_speed == 15) {
            return 9;
        }
        if (input_speed == 9) {
            return 8;
        }
        if (input_speed == 8) {
            return 6;
        }
        if (input_speed == 6) {
            return 3;
        }
        if (input_speed == 5) {
            return 3;
        }
        return input_speed;
    }

    private projectedCar perkiraan(int lane, int block, int speed, int damage, GameState gameState) {
        List<Lane[]> map = gameState.lanes;
        // List<Object> blocks = new ArrayList<>();
        int startBlock = map.get(0)[0].position.block;
        projectedCar output = new projectedCar();
        output.speed = speed;
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
            if (laneList[i].terrain == Terrain.WALL) {
                output.damage += 2;
                output.speed = 3;
                output.speed = prevSpeed(output.speed, output.damage);
            }
        }
        return output;
    }

    private int bestMove(projectedCar maju, projectedCar kiri, projectedCar kanan, Car myCar, GameState gameState) {
        // 1 - Maju, 2 - Kiri, 3- Kanan, 4 - LIZARD, 5 - Nyerang Orang
        if (maju.speed == kiri.speed && maju.speed == kanan.speed) {
            if (hasPowerUp(PowerUps.LIZARD, myCar.powerups)) {
                int pakeLizard = worthPakeLizard(myCar.position.lane, myCar.position.block, myCar.speed, gameState);
                if (pakeLizard > kiri.speed && pakeLizard > kanan.speed) {
                    return 4;
                }
            }
            if (maju.damage == kiri.damage && maju.damage == kanan.damage) {
                return 5;
            }
            if (maju.damage <= kiri.damage && maju.damage <= kanan.damage) {
                return 1;
            }
            if (myCar.position.lane == 3) {
                if (kiri.damage <= maju.damage && kiri.damage <= kanan.damage) {
                    return 2;
                }
                return 3;
            } else {
                if (kanan.damage <= maju.damage && kanan.damage <= kiri.damage) {
                    return 3;
                }
                return 2;
            }
        }
        if (maju.speed >= kiri.speed && maju.speed >= kanan.speed) {
            return 1;
        }
        if (myCar.position.lane == 3) {
            if (kiri.speed >= maju.speed && kiri.speed >= kanan.speed) {
                return 2;
            }
            return 3;
        } else {
            if (kanan.speed >= maju.speed && kanan.speed >= kiri.speed) {
                return 3;
            }
            return 2;
        }
    }

    int worthPakeLizard(int lane, int block, int speed, GameState gameState) {
        List<Lane[]> map = gameState.lanes;
        // List<Object> blocks = new ArrayList<>();
        int startBlock = map.get(0)[0].position.block;
        boolean found = false;
        Lane[] laneList = map.get(lane - 1);
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
            if (laneList[block - startBlock + speed].terrain != Terrain.MUD
                    && laneList[block - startBlock + speed].terrain != Terrain.WALL) {
                return 0;
            } else {
                return speed;
            }
        }
    }

}