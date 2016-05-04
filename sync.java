
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

	private boolean isPause = false;

	public synchronized void pause() {
		isPause = true;
	}

	public synchronized void resume() {
		isPause = false;
		notifyAll();
	}

	public synchronized void look() {
		while (isPause) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}