package com.example.mvmax.speakrecognition.speakrecognition.beatroot;

import java.util.ListIterator;

public class Agent {

	private static boolean debug = false;

	private static double POST_MARGIN_FACTOR = 0.3;

	private static double PRE_MARGIN_FACTOR = 0.15;
	
	private static final double INNER_MARGIN = 0.040;
	
	private static double MAX_CHANGE = 0.2;
		
	private static double CONF_FACTOR = 0.5;
	
	private static final double DEFAULT_CORRECTION_FACTOR = 50.0;
	
	private static final double DEFAULT_EXPIRY_TIME = 10.0;

	private static int idCounter = 0;
	
	private static double innerMargin;

	private static double correctionFactor;

	private static double expiryTime;
	
	private static double decayFactor;

	private double preMargin;

	private double postMargin;
	
	private int idNumber;
	
	double tempoScore;
	
	double phaseScore;
	
	double topScoreTime;
	
	/** The number of beats found by this Agent, including interpolated beats. */
	int beatCount;
	
	/** The current tempo hypothesis of the Agent, expressed as the beat period in seconds. */
	double beatInterval;

	/** The initial tempo hypothesis of the Agent, expressed as the beat period in seconds. */
	private double initialBeatInterval;
	
	/** The time of the most recent beat accepted by this Agent. */
	double beatTime;
	
	/** The list of Events (onsets) accepted by this Agent as beats, plus interpolated beats. */
	EventList events;

	/** Constructor: the work is performed by init()
	 *  @param ibi The beat period (inter-beat interval) of the Agent's tempo hypothesis.
	 */
	Agent(double ibi) {
		init(ibi);
	} // constructor

	/** Copy constructor.
	 *  @param clone The Agent to duplicate. */
	private Agent(Agent clone) {
		idNumber = idCounter++;
		phaseScore = clone.phaseScore;
		tempoScore = clone.tempoScore;
		topScoreTime = clone.topScoreTime;
		beatCount = clone.beatCount;
		beatInterval = clone.beatInterval;
		initialBeatInterval = clone.initialBeatInterval;
		beatTime = clone.beatTime;
		events = new EventList(clone.events);
		postMargin = clone.postMargin;
		preMargin = clone.preMargin;
	} // copy constructor

	/** Initialise all the fields of this Agent.
	 *  @param ibi The initial tempo hypothesis of the Agent.
	 */
	private void init(double ibi) {
		innerMargin = INNER_MARGIN;
		correctionFactor = DEFAULT_CORRECTION_FACTOR;
		expiryTime = DEFAULT_EXPIRY_TIME;
		decayFactor = 0;
		beatInterval = ibi;
		initialBeatInterval = ibi;
		postMargin = ibi * POST_MARGIN_FACTOR;
		preMargin = ibi * PRE_MARGIN_FACTOR;
		idNumber = idCounter++;
		phaseScore = 0.0;
		tempoScore = 0.0;
		topScoreTime = 0.0;
		beatCount = 0;
		beatTime = -1.0;
		events = new EventList();
	} // init()

	/** Output debugging information about this Agent, at the default (highest) level of detail.	 */
	public void print() {
		print(100);
	} // print()/0
	
	/** Output debugging information about this Agent.
	 *  @param level The level of detail in debugging
	 */
	void print(int level) {
		System.out.printf("\tAg#%4d: %5.3f", idNumber, beatInterval);
		if (level >= 1) {
			System.out.printf(
					"  Beat#%3d  Time=%7.3f  Score=%4.2f:P%4.2f:%3.1f",
					beatCount, beatTime, tempoScore, phaseScore,
					topScoreTime);
		}
		if (level >= 2)
			System.out.println();
		if (level >= 3)
			events.print();
	} // print()

	/** Accept a new Event as a beat time, and update the state of the Agent accordingly.
	 *  @param e The Event which is accepted as being on the beat.
	 *  @param err The difference between the predicted and actual beat times.
	 *  @param beats The number of beats since the last beat that matched an Event.
	 */
	private void accept(Event e, double err, int beats) {
		beatTime = e.keyDown;
		events.add(e);
		if (Math.abs(initialBeatInterval - beatInterval -
				err / correctionFactor) < MAX_CHANGE * initialBeatInterval)
			beatInterval += err / correctionFactor;// Adjust tempo
		beatCount += beats;
		double conFactor = 1.0 - CONF_FACTOR * err /
								(err>0? postMargin: -preMargin);
		if (decayFactor > 0) {
			double memFactor = 1. - 1. / threshold((double)beatCount,1,decayFactor);
			phaseScore = memFactor * phaseScore +
						 (1.0 - memFactor) * conFactor * e.salience;
		} else
			phaseScore += conFactor * e.salience;
		if (debug) {
			print(1);
			System.out.printf("  Err=" + (err<0?"":"+") + "%5.3f" +
						(Math.abs(err) > innerMargin ? '*':' ') + "%5.3f\n",
						err, conFactor);
		}
	} // accept()

	private double threshold(double value, double min, double max) {
		if (value < min)
			return min;
		if (value > max)
			return max;
		return value;
	}

	
	/** The given Event is tested for a possible beat time. The following situations can occur:
	 *  1) The Agent has no beats yet; the Event is accepted as the first beat.
	 *  2) The Event is beyond expiryTime seconds after the Agent's last 'confirming' beat; the Agent is terminated.
	 *  3) The Event is within the innerMargin of the beat prediction; it is accepted as a beat.
	 *  4) The Event is within the outerMargin's of the beat prediction; it is accepted as a beat by this Agent,
	 *     and a new Agent is created which doesn't accept it as a beat.
	 *  5) The Event is ignored because it is outside the windows around the Agent's predicted beat time.
	 * @param e The Event to be tested
	 * @param a The list of all agents, which is updated if a new agent is created.
	 * @return Indicate whether the given Event was accepted as a beat by this Agent.
	 */
	boolean considerAsBeat(Event e, AgentList a) {
		double err;
		if (beatTime < 0) {	// first event
			accept(e, 0, 1);
			return true;
		} else {			// subsequent events
			if (e.keyDown - events.l.getLast().keyDown > expiryTime) {
				phaseScore = -1.0;	// flag agent to be deleted
				return false;
			}
			double beats = Math.round((e.keyDown - beatTime) / beatInterval);
			err = e.keyDown - beatTime - beats * beatInterval;
			if ((beats > 0) && (-preMargin <= err) && (err <= postMargin)) {
				if (Math.abs(err) > innerMargin)	// Create new agent that skips this
					a.add(new Agent(this));	//  event (avoids large phase jump)
				accept(e, err, (int)beats);
				return true;
			}
		}
		return false;
	} // considerAsBeat()

	/** Interpolates missing beats in the Agent's beat track, starting from the beginning of the piece. */
	protected void fillBeats() {
		fillBeats(-1.0);
	} // fillBeats()/0

	/** Interpolates missing beats in the Agent's beat track.
	 *  @param start Ignore beats earlier than this start time 
	 */
	private void fillBeats(double start) {
		double prevBeat = 0, nextBeat, currentInterval, beats;
		ListIterator<Event> list = events.listIterator();
		if (list.hasNext()) {
			prevBeat = list.next().keyDown;
			// alt. to fill from 0:
			// prevBeat = Math.mod(list.next().keyDown, beatInterval);
			list.previous();
		}
		for ( ; list.hasNext(); list.next()) {
			nextBeat = list.next().keyDown;
			list.previous();
			beats = Math.round((nextBeat - prevBeat) / beatInterval - 0.01); //prefer slow
			currentInterval = (nextBeat - prevBeat) / beats;
			for ( ; (nextBeat > start) && (beats > 1.5); beats--) {
				prevBeat += currentInterval;
				if (debug)
					System.out.printf("Insert beat at: %8.3f (n=%1.0f)\n",
										prevBeat, beats - 1.0);
				list.add(newBeat(prevBeat, 0));	// more than once OK??
			}
			prevBeat = nextBeat;
		}
	} // fillBeats()
	
	/** Creates a new Event object representing a beat.
	 *  @param time The time of the beat in seconds
	 *  @param beatNum The index of the beat
	 *  @return The Event object representing the beat
	 */
	private Event newBeat(double time, int beatNum) {
		return new Event(time,time, time, 56, 64, beatNum, 0, 1);
	} // newBeat()

	/** Show detailed debugging output describing the beat tracking behaviour of this agent.
	 *  Calls showTracking()/1 with a default metrical level of 1.
	 *  @param allEvents An EventList of all onsets
	 */	
	public void showTracking(EventList allEvents) {
		showTracking(allEvents, 1.0);
	} // showTracking()/1

	/** Show detailed debugging output describing the beat tracking behaviour of this agent.
	 *  @param allEvents An EventList of all onsets
	 *  @param level The metrical level of beat tracking relative to the notated beat (used to count beats)
	 */
	private void showTracking(EventList allEvents, double level) {
		int count = 1, gapCount;
		double prevBeat, nextBeat, gap;
		ListIterator<Event> beats = events.listIterator();	// point to 1st beat
		ListIterator<Event> all = allEvents.listIterator();	// point to 1st event
		if (!beats.hasNext()) {
			System.err.println("No beats found");
			return;
		}
		prevBeat = events.l.getFirst().keyDown;
		// prevBeat = fmod(beats.next().keyDown, beatInterval);
		System.out.print("Beat  (IBI)   BeatTime   Other Events");
		boolean first = true;
		while (all.hasNext()) {	// print each real event
			Event currentEvent = all.next();
			Event currentBeat = null;
			while (beats.hasNext()) {	// if event was chosen as beat
				currentBeat = beats.next();
				if (currentBeat.keyDown > currentEvent.keyDown + Induction.clusterWidth)
					break;
				gap = currentBeat.keyDown - prevBeat;
				gapCount = (int) Math.round(gap / beatInterval);
				for (int j = 1; j < gapCount; j++) {	//empty beat(s) before event
					nextBeat = prevBeat + gap / gapCount;
					System.out.printf("\n%4d (%5.3f) [%7.3f ]",
						count++, nextBeat - prevBeat, nextBeat);
					prevBeat = nextBeat;
				}
				System.out.printf("\n%4d (%5.3f) ",
						count++, currentEvent.keyDown - prevBeat);
				prevBeat = currentBeat.keyDown;
				currentBeat = null;
				first = false;
			}
			if ((currentBeat != null) && (currentBeat.keyDown > currentEvent.keyDown)) {
				gap = currentBeat.keyDown - prevBeat;
				gapCount = (int) Math.round(gap / beatInterval);
				for (int j = 1; j < gapCount; j++) {	//empty beat(s) before event
					nextBeat = prevBeat + gap / gapCount;
					if (nextBeat >= currentEvent.keyDown)
						break;
					System.out.printf("\n%4d (%5.3f) [%7.3f ]",
						count++, nextBeat - prevBeat, nextBeat);
					prevBeat = nextBeat;
				}
				first = false;
			}
			if (first)	// for correct formatting of any initial (pre-beat) events
				System.out.print("\n                       ");
			System.out.printf("%8.3f%c ", currentEvent.keyDown,
					Math.abs(currentEvent.scoreBeat / level -
						Math.round(currentEvent.scoreBeat / level)) < 0.001?
					'*': ' ');
			first = false;
		}
		System.out.println();
	} // showTracking()

} // class Agent
