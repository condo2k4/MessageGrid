package message.grid;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A collection of individual variations of the same message. I.e. they have
 * slight variations in their base images or differing locations, but all still
 * convey the same message.
 * 
 * On request, the single message in this family with the smallest error can be
 * obtained for a given pattern.
 * 
 * @author kg249
 */
public class MessageFamily {
	
	private final List<Message> messages = new ArrayList<>();
	
	public MessageFamily() {}

	public MessageFamily add(Message e) {
		messages.add(e);
		return this;
	}
	
	public int getMinimumError(Pattern pattern) {
		Iterator<Message> it = messages.iterator();
		int minError = it.next().getError(pattern);
		while(it.hasNext()) {
			int err = it.next().getError(pattern);
			if(err<minError)
				minError = err;
		}
		return minError;
	}
	
	public Message getBestMessage(Pattern pattern) {
		Iterator<Message> it = messages.iterator();
		Message best = it.next();
		int minError = best.getError(pattern);
		while(it.hasNext()) {
			Message m = it.next();
			int err = m.getError(pattern);
			if(err<minError) {
				minError = err;
				best = m;
			}
		}
		return best;
	}
	
}
