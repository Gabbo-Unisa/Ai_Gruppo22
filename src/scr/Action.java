package scr;

/**
 * Created by IntelliJ IDEA. User: Administrator Date: Mar 4, 2008 Time: 3:35:31
 * PM
 */
public class Action {

	public double accelerate; // 0..1
	public double brake; // 0..1
	public double steering; // -1..1
	public double clutch; // 0..1
	public int gear; // -1..6
	public boolean restartRace;
	public int focus;	// ML Angolo di messa a fuoco desiderato in gradi [-90; 90],
							// impostare 360 se non si desidera alcuna lettura della messa a fuoco!

	public Action() {
		this.accelerate = 0.0;
		this.brake = 0.0;
		this.steering = 0.0;
		this.clutch = 0.0;
		this.gear = 0;
		this.restartRace = false;
		this.focus = 360;
	}

	public Action(Action action) {
		this.accelerate = action.accelerate;
		this.brake = action.brake;
		this.steering = action.steering;
		this.clutch = action.clutch;
		this.gear = action.gear;
		this.restartRace = action.restartRace;
		this.focus = action.focus;
	}

	public String toString() {
		limitValues();
		return "(accel " + accelerate + ") " + "(brake " + brake + ") " + "(clutch " + clutch + ") " + "(gear " + gear
				+ ") " + "(steer " + steering + ") " + "(meta " + (restartRace ? 1 : 0) + ") " + "(focus " + focus // ML
				+ ")";
	}

	public void limitValues() {
		accelerate = Math.max(0, Math.min(1, accelerate));
		brake = Math.max(0, Math.min(1, brake));
		clutch = Math.max(0, Math.min(1, clutch));
		steering = Math.max(-1, Math.min(1, steering));
		gear = Math.max(-1, Math.min(7, gear));

	}
}
