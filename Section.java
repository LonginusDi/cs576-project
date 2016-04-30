
// class for each section of the break down
public class Section{

	long startingFrame;
	long endingFrame;

	public Section(long start){
		this.startingFrame = start;
		this.endingFrame = start;
	}

	public void setEnd(long end){
		this.endingFrame = end;
	}

	public long getTotalFrame(){
		return (endingFrame - startingFrame + 1);
	}

}