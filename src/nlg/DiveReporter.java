package nlg;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import nlg.components.Realizer;
import nlg.components.SpeakerModel;
import nlg.components.SpeakerModel.Speakers;
import nlg.messages.Message;
import nlg.messages.MessageAscent;
import nlg.messages.MessageDiveDepth;
import nlg.messages.MessageDiveSummary;
import nlg.messages.MessageExcessBottomTime;
import nlg.messages.MessageNumberOfDives;
import nlg.messages.MessageSafetyStop;
import nlg.messages.MessageShallowDive;
import simplenlg.framework.DocumentElement;
import simplenlg.framework.NLGElement;
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
		String joyImgSrc = new File("img/joy.jpeg").toURI().toString();
		String angerImgSrc = new File("img/anger.jpg").toURI().toString();
		
		String corpusText = CorpusText.getText(diveFeatures.getDiveNo().intValue());
		
		SpeakerModel speakerModel = new SpeakerModel();
		String joyText = diveFeaturesToText(diveFeatures, speakerModel);
		joyText = joyText.replaceAll("!\\.", "!");
		joyText = joyText.replaceAll("\\?\\.", "?");
		
		speakerModel.update(Speakers.ANGER);
		String angerText = diveFeaturesToText(diveFeatures, speakerModel);
		angerText = angerText.replaceAll("!\\.", "!");
		angerText = angerText.replaceAll("\\?\\.", "?");
		
		return corpusText +
				"<br><br>" +
				"<table>" + 
					"<tr>" + 
						"<td><img src='" + joyImgSrc + "' height='100' width='100' style='vertical-align:middle;' /></td>" + 
						"<td>" + joyText + "</td>" +
					"</tr>" +
				"</table>" +
				"<br><br>" +
				"<table>" + 
					"<tr>" + 
						"<td><img src='" + angerImgSrc + "' height='100' width='100' style='vertical-align:middle;' /></td>" + 
						"<td>" + angerText + "</td>" +
					"</tr>" +
				"</table>";
	}
	
	private static String diveFeaturesToText(DiveFeatures diveFeatures, SpeakerModel speakerModel) {
		DocumentElement document = Realizer.factory.createParagraph();
		
		List<Message> allMessages = extractDiveMessages(diveFeatures);
		
		for(Message message: allMessages) {
			NLGElement element = message.getText(speakerModel);
			if(element != null)
				document.addComponent(Realizer.factory.createSentence(element));
		}
		
		return Realizer.realiser.realise(document).getRealisation();
	}
	
	private static List<Message> extractDiveMessages(DiveFeatures diveFeatures) {
		List<Message> messages = new ArrayList<Message>();
		
		messages.addAll(extractGeneralDiveMessages(diveFeatures));
		messages.addAll(extractDiveletMessages(diveFeatures.getFirstDiveletFeatures(), diveFeatures.getNumOfDivelets() > 1 ? 1 : 0));
		messages.addAll(extractDiveletMessages(diveFeatures.getSecondDiveletFeatures(), diveFeatures.getNumOfDivelets() > 1 ? 2 : 0));
		
		return messages;
	}
	
	private static List<Message> extractGeneralDiveMessages(DiveFeatures diveFeatures) {
		List<Message> messages = new ArrayList<Message>();
		
		if(diveFeatures.getNumOfDivelets() > 1) {
			MessageNumberOfDives message = new MessageNumberOfDives(diveFeatures.getNumOfDivelets(), diveFeatures.getSurfaceIntervalTime());
			messages.add(message);
		} else if(!diveFeatures.isRealDive()) {
			MessageShallowDive shallowDive = new MessageShallowDive();
			messages.add(shallowDive);
		}
		
		return messages;
	}
	
	private static List<Message> extractDiveletMessages(DiveletFeatures diveletFeatures, int diveNumber) {
		List<Message> messages = new ArrayList<Message>();
		
		if(diveletFeatures == null) return messages;
		
		MessageDiveSummary diveSummary = new MessageDiveSummary(diveletFeatures, diveNumber);
		messages.add(diveSummary);
		
		MessageDiveDepth diveDepth = new MessageDiveDepth(diveletFeatures.getDiveDepth(), diveletFeatures.getExcessDiveDepth());
		messages.add(diveDepth);
		
		MessageAscent ascent = new MessageAscent(diveletFeatures.getAscentSpeed());
		messages.add(ascent);
		
		if(diveletFeatures.isSafetyStopRequired()) {
			MessageSafetyStop safetyStop = new MessageSafetyStop();
			messages.add(safetyStop);
		}
		
		MessageExcessBottomTime excessBottomTime = new MessageExcessBottomTime(diveletFeatures.getExcessBottomTime());
		messages.add(excessBottomTime);
		
		return messages;
	} 

	
}
