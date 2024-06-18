/**
 * Represents an idle Userland process that calls an infinite loop of
 * cooperate() and Thread.sleep(50)
 * @author Matthew Welsh
 */
public class IdleProcess extends UserlandProcess {

    /**
     * Runs an infinite loop of cooperate() and Thread.sleep(50)
     * @author Matthew Welsh
     */
    @Override
    void main() {

//        while (true) {
//            cooperate(); //cooperates with the operating system
//            try {
//                Thread.sleep(50); //sleeps for 50 ms
//            } catch (Exception e) { }
//        }

        //For Paging Test
        cooperate(); //cooperates with the operating system
        try {
            Thread.sleep(50); //sleeps for 50 ms
        } catch (Exception e) { }
    }
}
