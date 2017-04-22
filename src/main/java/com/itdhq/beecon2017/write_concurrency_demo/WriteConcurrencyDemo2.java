package com.itdhq.beecon2017.write_concurrency_demo;

import java.io.Serializable;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * 
 */
public class WriteConcurrencyDemo2 extends DeclarativeWebScript {
    private static final Log logger = LogFactory.getLog(WriteConcurrencyDemo2.class);
    protected ServiceRegistry serviceRegistry;
    protected NodeService nodeService;
    protected Repository repositoryHelper;
    
    // Hardcoded names for demo, determined on the fly in real life
    protected String targetFolderName = "WRITE_CONCURRENCY_DEMO_2";
    protected QName targetFolderQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, targetFolderName);

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
        Map<String, Object> model = new HashMap<>();
        
        logger.info("Started new request processing");
        
        // Follow literally http://docs.alfresco.com/5.2/references/dev-extension-points-public-java-api.html
        NodeRef folderRef = serviceRegistry.getRetryingTransactionHelper().doInTransaction(
            new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>() {
                public NodeRef execute() throws Throwable {
                    // ... whatever, classify incoming data, determine correct location for it, etc ...
                    simulateWork(100L);

                    // Get target folder or create it if necessary
                    NodeRef rootFolderRef = repositoryHelper.getCompanyHome();
                    List<ChildAssociationRef> assocs = nodeService.getChildAssocs(
                            rootFolderRef, ContentModel.ASSOC_CONTAINS, targetFolderQName);

                    NodeRef targetFolderRef;
                    if(assocs.isEmpty()) {
                        logger.info("Creating new folder");
                        Map<QName, Serializable> props = new HashMap<>();
                        props.put(ContentModel.PROP_NAME, targetFolderName);
                        targetFolderRef = nodeService.createNode(rootFolderRef, 
                                ContentModel.ASSOC_CONTAINS, targetFolderQName, ContentModel.TYPE_FOLDER, props).getChildRef();
                    } else {
                        logger.info("Using existing folder");
                        targetFolderRef = assocs.get(0).getChildRef();
                    }

                    // ... whatever, real work with content ...
                    String nodeID = UUID.randomUUID().toString();
                    QName nodeQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, nodeID);
                    Map<QName, Serializable> props = new HashMap<>();
                    props.put(ContentModel.PROP_NAME, nodeID);
                    NodeRef ref = nodeService.createNode(targetFolderRef, 
                            ContentModel.ASSOC_CONTAINS, nodeQName, ContentModel.TYPE_CONTENT, props).getChildRef();
                    simulateWork(1000L);
                    return targetFolderRef;
                }
            }, false, true);
        
        model.put("folderRef", folderRef.toString());
        logger.info("Completed new request processing");
        
        return model;
    }
    
    protected void simulateWork(Long miliseconds) {
        try {
            Thread.sleep(miliseconds);
        } catch (InterruptedException e) {
            //
        }
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
        this.nodeService = serviceRegistry.getNodeService();
    }

    public void setRepositoryHelper(Repository repositoryHelper) {
        this.repositoryHelper = repositoryHelper;
    }   
}