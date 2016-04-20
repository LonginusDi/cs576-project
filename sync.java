
public class sync {

	int threadnum = 2;
	Object hold = new Object();

	public  void  checkSync(){
		synchronized(hold){
		threadnum --;

		if(threadnum > 0){
			try{
				hold.wait();
			}catch (InterruptedException e){}
		}
		else{
			hold.notifyAll();
			threadnum = 2;
		}
	}
		
	}
}