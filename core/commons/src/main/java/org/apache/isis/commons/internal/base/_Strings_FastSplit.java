package org.apache.isis.commons.internal.base;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.annotation.Nullable;

class _Strings_FastSplit {

	public static void splitThenAccept(
    		@Nullable final String input, 
    		final String separator, 
    		BiConsumer<String, String> onNonEmptySplit,
    		Consumer<String> onNonEmptyLhs,
    		Consumer<String> onNonEmptyRhs) {
    	
        if(_Strings.isEmpty(input)) {
        	// skip
            return;
        }
        
        // we have a non-empty string
        
        final int p = input.indexOf(separator);
        if(p<1){
			if(p==-1) {
				// separator not found
				onNonEmptyLhs.accept(input);
				return;
			}
			if(p==0) {
				// empty lhs in string
				if(input.length()>separator.length()) {
					onNonEmptyRhs.accept(input);
				}
				return;
			}
		}
        final int q = p + separator.length();
		if(q==input.length()) {
			// empty rhs
			onNonEmptyLhs.accept(input.substring(0, p));
			return;
		}
		onNonEmptySplit.accept(input.substring(0, p), input.substring(q));
    }
	
}
