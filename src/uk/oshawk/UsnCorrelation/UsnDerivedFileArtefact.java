package uk.oshawk.UsnCorrelation;

import org.sleuthkit.autopsy.casemodule.Case;
import org.sleuthkit.autopsy.casemodule.NoCurrentCaseException;
import org.sleuthkit.datamodel.BlackboardArtifact;
import org.sleuthkit.datamodel.BlackboardAttribute;
import org.sleuthkit.datamodel.BlackboardAttribute.TSK_BLACKBOARD_ATTRIBUTE_VALUE_TYPE;
import org.sleuthkit.datamodel.SleuthkitCase;
import org.sleuthkit.datamodel.TskCoreException;
import org.sleuthkit.datamodel.TskDataException;


public class UsnDerivedFileArtefact {
    // An artefact used to inform the investigator that a deleted file was added by the tool.
    // Documentation on artefacts: http://sleuthkit.org/sleuthkit/docs/jni-docs/4.3/mod_bbpage.html
    
    public static BlackboardArtifact.Type getArtefactType() {
        // Gets the artefact type, creating it if it does not exist.
        
        SleuthkitCase sleuthkitCase;
        
        try {
            sleuthkitCase = Case.getCurrentCaseThrows().getSleuthkitCase();
        } catch (NoCurrentCaseException e) {
            return null;
        }
        
        try {
            return sleuthkitCase.addBlackboardArtifactType("USN_DERIVED_FILE", "USN Derived File");
        } catch (TskCoreException | TskDataException e) { }
        
        try {
            return sleuthkitCase.getArtifactType("USN_DERIVED_FILE");
        } catch (TskCoreException e) {
            return null;
        }
    }
    
    public static BlackboardAttribute.Type getFilesystemIdAttributeType() {
        // Gets the file system ID attribute type, creating it if it does not exist.
        
        SleuthkitCase sleuthkitCase;
        
        try {
            sleuthkitCase = Case.getCurrentCaseThrows().getSleuthkitCase();
        } catch (NoCurrentCaseException e) {
            return null;
        }
        
        try {
            return sleuthkitCase.addArtifactAttributeType("USN_DERIVED_FILE_FILESYSTEM_ID", TSK_BLACKBOARD_ATTRIBUTE_VALUE_TYPE.LONG,"Filesystem ID");
        } catch (TskCoreException | TskDataException e) { }
        
        try {
            return sleuthkitCase.getAttributeType("USN_DERIVED_FILE_FILESYSTEM_ID");
        } catch (TskCoreException e) {
            return null;
        }
    }
    
    public static BlackboardAttribute.Type getIndexAttributeType() {
        // Gets the file index attribute type, creating it if it does not exist.
        
        SleuthkitCase sleuthkitCase;
        
        try {
            sleuthkitCase = Case.getCurrentCaseThrows().getSleuthkitCase();
        } catch (NoCurrentCaseException e) {
            return null;
        }
        
        try {
            return sleuthkitCase.addArtifactAttributeType("USN_DERIVED_FILE_INDEX", TSK_BLACKBOARD_ATTRIBUTE_VALUE_TYPE.LONG,"Index");
        } catch (TskCoreException | TskDataException e) { }
        
        try {
            return sleuthkitCase.getAttributeType("USN_DERIVED_FILE_INDEX");
        } catch (TskCoreException e) {
            return null;
        }
    }
    
    public static BlackboardAttribute.Type getSequenceNumberAttributeType() {
        // Gets the file sequence number attribute type, creating it if it does not exist.
        
        SleuthkitCase sleuthkitCase;
        
        try {
            sleuthkitCase = Case.getCurrentCaseThrows().getSleuthkitCase();
        } catch (NoCurrentCaseException e) {
            return null;
        }
        
        try {
            return sleuthkitCase.addArtifactAttributeType("USN_DERIVED_FILE_SEQUENCE_NUMBER", TSK_BLACKBOARD_ATTRIBUTE_VALUE_TYPE.INTEGER,"Sequence Number");
        } catch (TskCoreException | TskDataException e) { }
        
        try {
            return sleuthkitCase.getAttributeType("USN_DERIVED_FILE_SEQUENCE_NUMBER");
        } catch (TskCoreException e) {
            return null;
        }
    }
}
