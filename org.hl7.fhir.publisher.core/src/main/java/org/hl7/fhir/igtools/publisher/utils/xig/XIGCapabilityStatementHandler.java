package org.hl7.fhir.igtools.publisher.utils.xig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hl7.fhir.igtools.publisher.utils.xig.XIGHandler.PageContent;
import org.hl7.fhir.igtools.publisher.utils.xig.XIGInformation.UsageType;
import org.hl7.fhir.r5.model.CanonicalResource;
import org.hl7.fhir.r5.model.CanonicalType;
import org.hl7.fhir.r5.model.CapabilityStatement;
import org.hl7.fhir.r5.model.CapabilityStatement.CapabilityStatementRestComponent;
import org.hl7.fhir.r5.model.CapabilityStatement.CapabilityStatementRestResourceComponent;
import org.hl7.fhir.r5.model.CapabilityStatement.CapabilityStatementRestResourceOperationComponent;
import org.hl7.fhir.r5.model.CapabilityStatement.CapabilityStatementRestResourceSearchParamComponent;
import org.hl7.fhir.r5.model.CodeType;
import org.hl7.fhir.r5.model.ConceptMap.ConceptMapGroupComponent;
import org.hl7.fhir.r5.model.Enumerations.CapabilityStatementKind;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class XIGCapabilityStatementHandler extends XIGHandler {

  private XIGInformation info;

  public XIGCapabilityStatementHandler(XIGInformation info) {
    super();
    this.info = info;
  }

  public void fillOutJson(CapabilityStatement cs, JsonObject j) {
    if (cs.hasKind()) {    
      j.addProperty("kind", cs.getKind().toCode()); 
    }
    if (cs.hasFhirVersion()) {         
      j.addProperty("fhirVersion", cs.getFhirVersion().toCode()); 
    }
    
    for (CanonicalType g : cs.getInstantiates()) {
      if (g.hasValue()) {    
        if (!j.has("instantiates")) {
          j.add("instantiates", new JsonArray());
        }
        j.getAsJsonArray("instantiates").add(g.primitiveValue()); 
      }
    }
    for (CanonicalType g : cs.getImports()) {
      if (g.hasValue()) {    
        if (!j.has("imports")) {
          j.add("imports", new JsonArray());
        }
        j.getAsJsonArray("imports").add(g.primitiveValue()); 
      }
    }
    for (CodeType g : cs.getFormat()) {
      if (g.hasValue()) {    
        if (!j.has("formats")) {
          j.add("formats", new JsonArray());
        }
        j.getAsJsonArray("formats").add(g.primitiveValue()); 
      }
    }
    for (CodeType g : cs.getPatchFormat()) {
      if (g.hasValue()) {    
        if (!j.has("formats")) {
          j.add("formats", new JsonArray());
        }
        j.getAsJsonArray("formats").add(g.primitiveValue()); 
      }
    }
    for (CodeType g : cs.getAcceptLanguage()) {
      if (g.hasValue()) {    
        if (!j.has("languages")) {
          j.add("languages", new JsonArray());
        }
        j.getAsJsonArray("languages").add(g.primitiveValue()); 
      }
    }
    
    for (CanonicalType g : cs.getImplementationGuide()) {
      if (g.hasValue()) {    
        if (!j.has("implementationGuides")) {
          j.add("implementationGuides", new JsonArray());
        }
        j.getAsJsonArray("implementationGuides").add(g.primitiveValue()); 
      }
    }
    
  }
  
  
  public PageContent makeCapabilityStatementPage(CapabilityStatementKind kind, String title, String realm) {
    List<CapabilityStatement> list = new ArrayList<>();
    for (CanonicalResource cr : info.getResources().values()) {
      if (meetsRealm(cr, realm)) {
        if (cr instanceof CapabilityStatement) {
          CapabilityStatement cs = (CapabilityStatement) cr;
          boolean ok = cs.getKind() == kind;
          if (ok) {
            list.add(cs);
          }
        }
      }
    }
    if (list.isEmpty() && kind != null) {
      return null;
    }

    Collections.sort(list, new CanonicalResourceSorter());
    StringBuilder b = new StringBuilder();

    b.append("<table class=\"\">\r\n");
    crTrHeaders(b, false);
    DuplicateTracker dt = new DuplicateTracker();
    for (CapabilityStatement cs : list) {
      crTr(b, dt, cs, 0);      
    }
    b.append("</table>\r\n");

    return new PageContent(title+" ("+list.size()+")", b.toString());
  }

  public static void buildUsages(XIGInformation info, CapabilityStatement cs) {
    for (CanonicalType ct : cs.getImports()) {
      info.recordUsage(cs, ct.getValue(), UsageType.CS_IMPORTS);
    }
    for (CanonicalType ct : cs.getInstantiates()) {
      info.recordUsage(cs, ct.getValue(), UsageType.CS_IMPORTS);
    }
    for (CanonicalType ct : cs.getImplementationGuide()) {
      info.recordUsage(cs, ct.getValue(), UsageType.CS_IMPORTS);
    }
    for (CapabilityStatementRestComponent tr1 : cs.getRest()) {
      for (CapabilityStatementRestResourceSearchParamComponent t : tr1.getSearchParam()) {
        info.recordUsage(cs, t.getDefinition(), UsageType.CS_IMPORTS);
      }
      for (CapabilityStatementRestResourceOperationComponent t : tr1.getOperation()) {
        info.recordUsage(cs, t.getDefinition(), UsageType.CS_IMPORTS);
      }
      for (CapabilityStatementRestResourceComponent tr : tr1.getResource()) {
        info.recordUsage(cs, tr.getProfile(), UsageType.CS_PROFILE);
        for (CanonicalType t : tr.getSupportedProfile()) {
          info.recordUsage(cs, t.getValue(), UsageType.CS_PROFILE);
        }
        for (CapabilityStatementRestResourceSearchParamComponent t : tr.getSearchParam()) {
          info.recordUsage(cs, t.getDefinition(), UsageType.CS_IMPORTS);
        }
        for (CapabilityStatementRestResourceOperationComponent t : tr.getOperation()) {
          info.recordUsage(cs, t.getDefinition(), UsageType.CS_IMPORTS);
        }
      }
    }
  }
}
