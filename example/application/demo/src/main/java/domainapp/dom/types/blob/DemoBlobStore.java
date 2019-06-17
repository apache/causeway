package domainapp.dom.types.blob;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpSession;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.isis.applib.value.Blob;
import org.apache.isis.core.runtime.system.context.IsisContext;

import lombok.val;

@Singleton
public class DemoBlobStore {
	
	@Inject HttpSession session;
	
	public void put(UUID uuid, Blob blob) {
		if(blob==null) {
			return;
		}
		session.setAttribute(uuid.toString(), blob);
	}

	public Blob get(UUID uuid) {
		if(uuid==null) {
			return null;
		}
		return (Blob) session.getAttribute(uuid.toString());
	}
	
	private static DemoBlobStore current() {
		return IsisContext.getServiceRegistry().lookupServiceElseFail(DemoBlobStore.class);
	}
	
	// -- JAXB ADAPTER
	
    public static final class BlobAdapter extends XmlAdapter<String, Blob> {

    	
		@Override
		public Blob unmarshal(String data) throws Exception {
		    if(data==null) {
                return null;
            }
		    val uuid = UUID.fromString(data);
		    return DemoBlobStore.current().get(uuid);
		}
		
		@Override
		public String marshal(Blob blob) throws Exception {
			if(blob==null) {
                return null;
            }
		    val uuid = UUID.randomUUID();
			DemoBlobStore.current().put(uuid, blob);
			return uuid.toString();
		}
    	
    }
	
	
}
