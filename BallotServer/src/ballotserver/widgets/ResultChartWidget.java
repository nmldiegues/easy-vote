package ballotserver.widgets;

import ballotserver.views.CandidateVotesView;
import ballotserver.views.ElectionResultView;
import java.lang.String;

import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ResultChartWidget extends VerticalPanel {
	private Image chart;
	private String finalUrl = "http://chart.apis.google.com/chart?chs=400x250&cht=p";
	
	public ResultChartWidget(ElectionResultView countView) {

		
		/* Add chart data */
		finalUrl += "&chd=t:";
		
		int i = countView.getCandidatesResults().size();
		float votePercentage;
		String auxUrl = "&chdl=";
		//String formatString;
		for(CandidateVotesView candidate : countView.getCandidatesResults()) {
			
			/* Add values */
			votePercentage = (candidate.getNumberVotes()*100)/(float)countView.getTotalVotes();
			//formatString = String.format("%4.2f", votePercentage);
			
			finalUrl += formatString("" + votePercentage);
			
			/* Add Labels */
			auxUrl += "Candidate+" + candidate.getUniqueIdentifier() + "+(" + formatString("" + votePercentage) + "%)";

			finalUrl += ",";
			auxUrl += "|";
			
		}
		
		/* Add Blank Votes Percentage */
		votePercentage = (countView.getBlankVotes()*100)/(float)countView.getTotalVotes();
		
		finalUrl += formatString("" + votePercentage);
		auxUrl += "Blank+Votes+(" + formatString("" + votePercentage)+ ")";
		
		finalUrl += auxUrl;
		
		/* Chart Title */
		finalUrl += "&chtt=Elections+Result+-+Election+" + countView.getElectionId();
		
		chart = new Image(finalUrl);
		this.add(chart);
	}
	
	public String formatString(String s) {
		if(s.length() < 7) {
			return s;
		} else {
			return s.substring(0, 6);
		}
	}
}
