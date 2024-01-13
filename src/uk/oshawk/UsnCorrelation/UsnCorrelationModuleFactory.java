package uk.oshawk.UsnCorrelation;

import org.openide.util.lookup.ServiceProvider;
import org.sleuthkit.autopsy.ingest.DataSourceIngestModule;
import org.sleuthkit.autopsy.ingest.IngestModuleFactory;
import org.sleuthkit.autopsy.ingest.IngestModuleFactoryAdapter;
import org.sleuthkit.autopsy.ingest.IngestModuleIngestJobSettings;

@ServiceProvider(service = IngestModuleFactory.class)
public class UsnCorrelationModuleFactory extends IngestModuleFactoryAdapter {
    // Used to return basic information about the ingest (file information recovery) module.
    
    @Override
    public String getModuleDisplayName() {
        // Returns the name of the module from a constant.
        
        return UsnUtil.NAME;
    }

    @Override
    public String getModuleDescription() {
        // Returns the description of the module from a constant.
        
        return UsnUtil.DESCRIPTION;
    }

    @Override
    public String getModuleVersionNumber() {
        // Returns the version of the module from a constant.
        
        return UsnUtil.VERSION;
    }
    
    @Override
    public boolean isDataSourceIngestModuleFactory() {
        // Returns that this class can create an ingest module.
        
        return true;
    }
    
    @Override
    public DataSourceIngestModule createDataSourceIngestModule(IngestModuleIngestJobSettings ingestOptions) {
        // Returns an instance of the ingest module.
        
        return new UsnCorrelationDataSourceInjestModule();
    }
}
