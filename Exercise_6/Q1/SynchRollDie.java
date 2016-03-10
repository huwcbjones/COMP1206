/**
 * {DESCRIPTION}
 *
 * @author Huw Jones
 * @since 10/03/2016
 */
public class SynchRollDie extends RollDie {

    private boolean isValueSet = false;

    public SynchRollDie(Dice dice) {
        super(dice);
    }

    @Override

    public void run(){
        this.isValueSet = false;
        super.run();
        synchronized (this.dice){
            this.dice.notify();
            this.isValueSet = true;
        }
    }

    public int getVal(){
        if(!isValueSet) {
            synchronized (this.dice) {
                try {
                    this.dice.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        return dice.getValue();
    }
}
