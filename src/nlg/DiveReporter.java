package nlg;

import java.util.ArrayList;
import java.util.List;

import nlg.components.Realizer;
import nlg.messages.Message;
import nlg.messages.MessageAscent;
import nlg.messages.MessageDiveDepth;
import nlg.messages.MessageNumberOfDives;
import analytics.DiveFeatures;
import analytics.DiveletFeatures;

public class DiveReporter implements Reporter {
	DiveFeatures diveFeatures;

	public DiveReporter(DiveFeatures diveFeatures) {
		this.diveFeatures = diveFeatures;
	}

	/* (non-Javadoc)
	 * @see scuba.Reporter#generateText()
	 */
	@Override
	public String generateText() {
		String corpusText = CorpusText.getText(diveFeatures.getDiveNo().intValue());
		return corpusText+"<br><b>Computer Text:</b><br>"+diveFeatures.toString()+"<br><br>"+diveFeaturesToText(diveFeatures);
	}
	
	private static String diveFeaturesToText(DiveFeatures diveFeatures) {
		StringBuilder sb = new StringBuilder();
		
		List<Message> allMessages = extractDiveMessages(diveFeatures);
		
		for(Message message: allMessages) {
			sb.append(Realizer.realiser.realiseSentence(message.getText()));
		}
		
		return sb.toString();
	}
	
	private static List<Message> extractDiveMessages(DiveFeatures diveFeatures) {
		List<Message> messages = new ArrayList<Message>();
		
		messages.addAll(extractGeneralDiveMessages(diveFeatures));
		messages.addAll(extractDiveletMessages(diveFeatures.getFirstDiveletFeatures()));
		messages.addAll(extractDiveletMessages(diveFeatures.getSecondDiveletFeatures()));
		
		return messages;
	}
	
	private static List<Message> extractGeneralDiveMessages(DiveFeatures diveFeatures) {
		List<Message> messages = new ArrayList<Message>();
		
		MessageNumberOfDives message = new MessageNumberOfDives(diveFeatures.getNumOfDivelets());
		
		messages.add(message);
		
		return messages;
	}
	
	private static List<Message> extractDiveletMessages(DiveletFeatures diveletFeatures) {
		List<Message> messages = new ArrayList<Message>();
		
		MessageDiveDepth diveDepth = new MessageDiveDepth(diveletFeatures.getDiveDepth());
		MessageAscent ascent = new MessageAscent(diveletFeatures.getAscentSpeed());
		
		messages.add(diveDepth);
		messages.add(ascent);
		
		return messages;
	} 

	
}
