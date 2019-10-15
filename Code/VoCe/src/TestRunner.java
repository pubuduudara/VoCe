import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public class TestRunner {

	
	public static void main(String[] args) {
		Result result = JUnitCore.runClasses(TestVoipClassMethods.class,TestPacketHandler.class);
		
		for(Failure failure : result.getFailures()) System.out.println(failure.toString());
		
		System.out.println("Testing was sucessful : "+ result.wasSuccessful());
	}
}
