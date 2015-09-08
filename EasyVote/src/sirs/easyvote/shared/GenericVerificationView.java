package sirs.easyvote.shared;

import java.io.Serializable;

public class GenericVerificationView implements Serializable{

	private static final long serialVersionUID = 1963169419108206044L;

	private int chosenValue;
	private int receivedValue;
	
	public GenericVerificationView(){
		
	}
	
	public GenericVerificationView(int chosenValue, int receivedValue){
		this.chosenValue = chosenValue;
		this.receivedValue = receivedValue;
	}

	public int getChosenValue() {
		return chosenValue;
	}

	public int getReceivedValue() {
		return receivedValue;
	}
	
	public boolean verifyVote(){
		return chosenValue == receivedValue;
	}
	
	
	
}
