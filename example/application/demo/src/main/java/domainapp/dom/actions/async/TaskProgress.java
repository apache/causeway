package domainapp.dom.actions.async;

import java.util.concurrent.atomic.LongAdder;

import lombok.Data;
import lombok.val;

@Data(staticConstructor="of")
public class TaskProgress {
    
    private final LongAdder stepsProgressed;
    private final long totalSteps;
    
    public double progressedRelative() {
        val totalReciprocal = 1./totalSteps;
        
        return stepsProgressed.doubleValue() * totalReciprocal;
    }
    
    public double progressedPercent() {
        return Math.min(progressedRelative()*100., 100.);
    }
    
    public int progressedPercentAsInt() {
        return (int) Math.round(progressedPercent());
    }
    
    public String toHtmlProgressBar() {
        val percent = progressedPercentAsInt();

        return 
        "<div class=\"progress\">" + 
        "  <div class=\"progress-bar\" role=\"progressbar\" style=\"width: "+percent+"%\" aria-valuenow=\""+percent+"\" aria-valuemin=\"0\" aria-valuemax=\"100\"></div>" + 
        "</div>";
    }
    
    
}
