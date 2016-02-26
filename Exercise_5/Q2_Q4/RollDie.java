import java.util.Random;

/**
 * Rolls a Die
 *
 * @author Huw Jones
 * @since 26/02/2016
 */
public class RollDie implements Runnable {

    private Dice dice;

    public RollDie(Dice dice) {
        this.dice = dice;
    }

    @Override
    public void run() {
        // Guarantee minimum of 10 rolls, max of 20
        int numRolls = 10 + new Random().nextInt(10);

        for(int i = 1; i <= numRolls; i++){
            if(Thread.interrupted()){
                break;
            }

            try {
                Thread.sleep(1000 * i / (i + 20));
            } catch (InterruptedException e) {
                break;
            }

            int previousValue = dice.getValue();
            int newValue;

            // Prevent duplicate values
            do {
                newValue = 1 + new Random().nextInt(5);
            } while(newValue == previousValue);

            dice.updateVal(newValue);
            System.out.println(dice.getValue());
        }
        System.out.println("Roll finished");
    }
}
