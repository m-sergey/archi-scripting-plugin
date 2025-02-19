/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package com.archimatetool.script.dom.model;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.osgi.util.NLS;

import com.archimatetool.editor.ArchiPlugin;
import com.archimatetool.editor.diagram.commands.DiagramModelObjectOutlineAlphaCommand;
import com.archimatetool.editor.preferences.IPreferenceConstants;
import com.archimatetool.editor.ui.factory.IArchimateElementUIProvider;
import com.archimatetool.editor.ui.factory.IGraphicalObjectUIProvider;
import com.archimatetool.editor.ui.factory.IObjectUIProvider;
import com.archimatetool.editor.ui.factory.ObjectUIFactory;
import com.archimatetool.model.IArchimateFactory;
import com.archimatetool.model.IArchimatePackage;
import com.archimatetool.model.IBounds;
import com.archimatetool.model.IDiagramModelArchimateObject;
import com.archimatetool.model.IDiagramModelContainer;
import com.archimatetool.model.IDiagramModelImageProvider;
import com.archimatetool.model.IDiagramModelObject;
import com.archimatetool.model.IIconic;
import com.archimatetool.model.ITextAlignment;
import com.archimatetool.model.ITextPosition;
import com.archimatetool.script.ArchiScriptException;
import com.archimatetool.script.commands.CommandHandler;
import com.archimatetool.script.commands.DeleteDiagramModelObjectCommand;
import com.archimatetool.script.commands.ScriptCommandWrapper;
import com.archimatetool.script.commands.SetCommand;

/**
 * Diagram Model Object wrapper proxy
 * 
 * @author Phillip Beauvoir
 */
public class DiagramModelObjectProxy extends DiagramModelComponentProxy {
    
    DiagramModelObjectProxy(IDiagramModelObject object) {
        super(object);
    }
    
    /**
     * Add an Archimate element to this diagram object and return the new diagram object
     */
    public DiagramModelObjectProxy add(ArchimateElementProxy elementProxy, int x, int y, int width, int height) {
        if(getEObject() instanceof IDiagramModelContainer) {
            return ModelFactory.addArchimateDiagramObject((IDiagramModelContainer)getEObject(), elementProxy.getEObject(), x, y, width, height, false);
        }
        
        throw new ArchiScriptException(Messages.DiagramModelObjectProxy_0);
    }
    
    /**
     * Create and add a diagram object and return the diagram object proxy
     */
    public DiagramModelObjectProxy createObject(String type, int x, int y, int width, int height) {
        if(getEObject() instanceof IDiagramModelContainer) {
            return ModelFactory.createDiagramObject((IDiagramModelContainer)getEObject(), type, x, y, width, height, false);
        }
        
        throw new ArchiScriptException(Messages.DiagramModelObjectProxy_1);
    }
    
    /**
     * Create and add a view reference to another view and return the diagram object proxy
     */
    public DiagramModelReferenceProxy createViewReference(DiagramModelProxy dmRef, int x, int y, int width, int height) {
        if(getEObject() instanceof IDiagramModelContainer) {
            return ModelFactory.createViewReference((IDiagramModelContainer)getEObject(), dmRef.getEObject(), x, y, width, height, false);
        }
        
        throw new ArchiScriptException(Messages.DiagramModelObjectProxy_1);
    }

    @Override
    protected IDiagramModelObject getEObject() {
        return (IDiagramModelObject)super.getEObject();
    }
    
    @Override
    protected boolean isArchimateConcept() {
        return getEObject() instanceof IDiagramModelArchimateObject;
    }
    
    public Map<String, Object> getBounds() {
        IBounds b = getEObject().getBounds();
        
        Map<String, Object> map = new HashMap<>();
        map.put("x", b.getX()); //$NON-NLS-1$
        map.put("y", b.getY()); //$NON-NLS-1$
        map.put("width", b.getWidth()); //$NON-NLS-1$
        map.put("height", b.getHeight()); //$NON-NLS-1$
        
        return map;
    }
    
    public DiagramModelObjectProxy setBounds(Map<?, ?> map) {
        int x = ModelUtil.getIntValueFromMap(map, "x", getEObject().getBounds().getX()); //$NON-NLS-1$
        int y = ModelUtil.getIntValueFromMap(map, "y", getEObject().getBounds().getY()); //$NON-NLS-1$
        int width = ModelUtil.getIntValueFromMap(map, "width", getEObject().getBounds().getWidth()); //$NON-NLS-1$
        int height = ModelUtil.getIntValueFromMap(map, "height", getEObject().getBounds().getHeight()); //$NON-NLS-1$
        
        if(width == -1) {
            width = ArchiPlugin.INSTANCE.getPreferenceStore().getInt(IPreferenceConstants.DEFAULT_ARCHIMATE_FIGURE_WIDTH);
        }
        
        if(height == -1) {
            height = ArchiPlugin.INSTANCE.getPreferenceStore().getInt(IPreferenceConstants.DEFAULT_ARCHIMATE_FIGURE_HEIGHT);
        }

        IBounds bounds = IArchimateFactory.eINSTANCE.createBounds(x, y, width, height);
        CommandHandler.executeCommand(new SetCommand(getEObject(), IArchimatePackage.Literals.DIAGRAM_MODEL_OBJECT__BOUNDS, bounds));
        
        return this;
    }
    
    /**
     * @return child node diagram objects of this diagram object (if any)
     */
    @Override
    protected EObjectProxyCollection children() {
        EObjectProxyCollection list = new EObjectProxyCollection();
        
        if(getEObject() instanceof IDiagramModelContainer) {
            for(IDiagramModelObject dmo : ((IDiagramModelContainer)getEObject()).getChildren()) {
                list.add(EObjectProxy.get(dmo));
            }
        }
        
        return list;
    }
    
    @Override
    protected EObjectProxyCollection find() {
        // We don't include relationships
        EObjectProxyCollection all = super.find();
        return all.filter(IModelConstants.ELEMENT);
    }
    
    public String getFillColor() {
        return getEObject().getFillColor();
    }
    
    public DiagramModelObjectProxy setFillColor(String value) {
        checkColorValue(value); // check correct color value
        // Set color. A null value is allowed
        CommandHandler.executeCommand(new SetCommand(getEObject(), IArchimatePackage.Literals.DIAGRAM_MODEL_OBJECT__FILL_COLOR, value));
        return this;
    }
    
    public int getOpacity() {
        return getEObject().getAlpha();
    }
    
    public DiagramModelObjectProxy setOpacity(int value) {
        if(value < 0) {
            value = 0;
        }
        if(value > 255) {
            value = 255;
        }
        
        CommandHandler.executeCommand(new SetCommand(getEObject(), IArchimatePackage.Literals.DIAGRAM_MODEL_OBJECT__ALPHA, value));
        
        return this;
    }

    public int getOutlineOpacity() {
        return getEObject().getLineAlpha();
    }
    
    public DiagramModelObjectProxy setOutlineOpacity(int value) {
        if(value < 0) {
            value = 0;
        }
        if(value > 255) {
            value = 255;
        }
        
        CommandHandler.executeCommand(new ScriptCommandWrapper(new DiagramModelObjectOutlineAlphaCommand(getEObject(), value), getEObject()));
        
        return this;
    }

    public int getGradient() {
        return getEObject().getGradient();
    }
    
    public DiagramModelObjectProxy setGradient(int value) {
        if(value < -1 || value > 3) {
            value = -1;
        }
        
        if(ModelUtil.shouldExposeFeature(getEObject(), IDiagramModelObject.FEATURE_GRADIENT)) {
            CommandHandler.executeCommand(new SetCommand(getEObject(), IDiagramModelObject.FEATURE_GRADIENT, value, IDiagramModelObject.FEATURE_GRADIENT_DEFAULT));
        }
        
        return this;
    }
    
    public DiagramModelObjectProxy setFigureType(int value) {
        // If this is an ArchiMate type...
        if(isArchimateConcept()) {
            // And we support alternate figures for this diagram model object...
            IObjectUIProvider provider = ObjectUIFactory.INSTANCE.getProviderForClass(getConcept().getEObject().eClass());
            if(provider instanceof IArchimateElementUIProvider && ((IArchimateElementUIProvider)provider).hasAlternateFigure()) {
                if(value < 0) {
                    value = 0;
                }
                if(value > 1) {
                    value = 1;
                }          
                CommandHandler.executeCommand(new SetCommand(getEObject(), IArchimatePackage.Literals.DIAGRAM_MODEL_ARCHIMATE_OBJECT__TYPE, value));
            }
        }
        
        return this;
    }
    
    public int getFigureType() {
        if(isArchimateConcept()) {
            return ((IDiagramModelArchimateObject)getEObject()).getType();
        }
        
        return 0;
    }
    
    public DiagramModelObjectProxy setTextAlignment(int alignment) {
        if(alignment != ITextAlignment.TEXT_ALIGNMENT_CENTER
                && alignment != ITextAlignment.TEXT_ALIGNMENT_LEFT
                && alignment != ITextAlignment.TEXT_ALIGNMENT_RIGHT) {
            throw new ArchiScriptException(Messages.DiagramModelObjectProxy_2);
        }
        
        CommandHandler.executeCommand(new SetCommand(getEObject(), IArchimatePackage.Literals.TEXT_ALIGNMENT__TEXT_ALIGNMENT, alignment));
        
        return this;
    }
    
    public Object getTextAlignment() {
        return getEObject().getTextAlignment();
    }
    
    public DiagramModelObjectProxy setTextPosition(int position) {
        if(getEObject() instanceof ITextPosition) {
            if(position < ITextPosition.TEXT_POSITION_TOP || position > ITextPosition.TEXT_POSITION_BOTTOM) {
                position = ITextPosition.TEXT_POSITION_TOP;
            }
            CommandHandler.executeCommand(new SetCommand(getEObject(), IArchimatePackage.Literals.TEXT_POSITION__TEXT_POSITION, position));
        }
        
        return this;
    }
    
    public Object getTextPosition() {
        if(getEObject() instanceof ITextPosition) {
            return ((ITextPosition)getEObject()).getTextPosition();
        }
        
        return null;
    }
    
    public Object getShowIcon() {
        return getEObject().getIconVisibleState();
    }
    
    public DiagramModelObjectProxy setShowIcon(int value) {
        // Only for objects that have an icon
        IObjectUIProvider provider = ObjectUIFactory.INSTANCE.getProvider(getEObject());
        
        if(provider instanceof IGraphicalObjectUIProvider && ((IGraphicalObjectUIProvider)provider).hasIcon()) {
            // Bounds checking
            if(value < IDiagramModelObject.ICON_VISIBLE_IF_NO_IMAGE_DEFINED || value > IDiagramModelObject.ICON_VISIBLE_NEVER) {
                value = IDiagramModelObject.FEATURE_ICON_VISIBLE_DEFAULT;
            }
            
            CommandHandler.executeCommand(new SetCommand(getEObject(), IDiagramModelObject.FEATURE_ICON_VISIBLE, value, IDiagramModelObject.FEATURE_ICON_VISIBLE_DEFAULT));
        }
        
        return this;
    }
    
    public Object getImageSource() {
        if(isArchimateConcept() &&
                ModelUtil.shouldExposeFeature(getEObject(), IDiagramModelArchimateObject.FEATURE_IMAGE_SOURCE)) {
            return ((IDiagramModelArchimateObject)getEObject()).getImageSource();
        }
        
        return -1;
    }
    
    public DiagramModelObjectProxy setImageSource(int value) {
        if(isArchimateConcept() &&
                ModelUtil.shouldExposeFeature(getEObject(), IDiagramModelArchimateObject.FEATURE_IMAGE_SOURCE)) {
            
            // Bounds checking
            if(value < IDiagramModelArchimateObject.IMAGE_SOURCE_PROFILE || value > IDiagramModelArchimateObject.IMAGE_SOURCE_CUSTOM) {
                value = IDiagramModelArchimateObject.FEATURE_IMAGE_SOURCE_DEFAULT;
            }
            
            CommandHandler.executeCommand(new SetCommand(getEObject(), IDiagramModelArchimateObject.FEATURE_IMAGE_SOURCE, value, IDiagramModelArchimateObject.FEATURE_IMAGE_SOURCE_DEFAULT));
        }
        
        return this;
    }
    
    public Object getImagePosition() {
        if(getEObject() instanceof IIconic &&
                ModelUtil.shouldExposeFeature(getEObject(), IArchimatePackage.Literals.DIAGRAM_MODEL_IMAGE_PROVIDER__IMAGE_PATH.getName())) {
            return ((IIconic)getEObject()).getImagePosition();
        }
        
        return -1;
    }
    
    public DiagramModelObjectProxy setImagePosition(int value) {
        if(getEObject() instanceof IIconic &&
                ModelUtil.shouldExposeFeature(getEObject(), IArchimatePackage.Literals.DIAGRAM_MODEL_IMAGE_PROVIDER__IMAGE_PATH.getName())) {
            // Bounds checking
            if(value < IIconic.ICON_POSITION_TOP_LEFT || value > IIconic.ICON_POSITION_FILL) {
                value = IIconic.ICON_POSITION_TOP_LEFT;
            }
            
            CommandHandler.executeCommand(new SetCommand(getEObject(), IArchimatePackage.Literals.ICONIC__IMAGE_POSITION, value));
        }
        
        return this;
    }
    
    public Map<String, Object> getImage() {
        if(getEObject() instanceof IDiagramModelImageProvider &&
                ModelUtil.shouldExposeFeature(getEObject(), IArchimatePackage.Literals.DIAGRAM_MODEL_IMAGE_PROVIDER__IMAGE_PATH.getName())) {
            return ModelFactory.createImageObject(getArchimateModel(), ((IDiagramModelImageProvider)getEObject()).getImagePath());
        }
        
        return null;
    }
    
    public DiagramModelObjectProxy setImage(Map<?, ?> map) {
        if(getEObject() instanceof IDiagramModelImageProvider &&
                ModelUtil.shouldExposeFeature(getEObject(), IArchimatePackage.Literals.DIAGRAM_MODEL_IMAGE_PROVIDER__IMAGE_PATH.getName())) {
            
            String imagePath = map != null ? ModelUtil.getStringValueFromMap(map, "path", null) : null; //$NON-NLS-1$
            
            // If imagePath is not null check that the ArchiveManager has this image
            if(imagePath != null && !ModelUtil.hasImage(getArchimateModel(), imagePath)) {
                throw new ArchiScriptException(NLS.bind(Messages.ModelFactory_12, imagePath));
            }
            
            CommandHandler.executeCommand(new SetCommand(getEObject(), IArchimatePackage.Literals.DIAGRAM_MODEL_IMAGE_PROVIDER__IMAGE_PATH, imagePath));
        }
        
        return this;
    }
    
    @Override
    protected Object attr(String attribute) {
        switch(attribute) {
            case BOUNDS:
                return getBounds();
            case FILL_COLOR:
                return getFillColor();
            case OPACITY:
                return getOpacity();
            case OUTLINE_OPACITY:
                return getOutlineOpacity();
            case GRADIENT:
                return getGradient();
            case FIGURE_TYPE:
                return getFigureType();
            case TEXT_ALIGNMENT:
                return getTextAlignment();
            case TEXT_POSITION:
                return getTextPosition();
            case SHOW_ICON:
                return getShowIcon();
            case IMAGE_SOURCE:
                return getImageSource();
            case IMAGE_POSITION:
                return getImagePosition();
            case IMAGE:
                return getImage();
        }
        
        return super.attr(attribute);
    }
    
    @Override
    protected EObjectProxy attr(String attribute, Object value) {
        switch(attribute) {
            case BOUNDS:
                if(value instanceof Map) {
                    return setBounds((Map<?, ?>)value);
                }
                break;
            case FILL_COLOR:
                if(value == null || value instanceof String) {
                    return setFillColor((String)value);
                }
                break;
            case OPACITY:
                if(value instanceof Integer) {
                    return setOpacity((int)value);
                }
                break;
            case OUTLINE_OPACITY:
                if(value instanceof Integer) {
                    return setOutlineOpacity((int)value);
                }
                break;
            case GRADIENT:
                if(value instanceof Integer) {
                    return setGradient((int)value);
                }
                break;
            case FIGURE_TYPE:
                if(value instanceof Integer) {
                    return setFigureType((int)value);
                }
                break;
            case TEXT_ALIGNMENT:
                if(value instanceof Integer) {
                    return setTextAlignment((int)value);
                }
                break;
            case TEXT_POSITION:
                if(value instanceof Integer) {
                    return setTextPosition((int)value);
                }
                break;
            case SHOW_ICON:
                if(value instanceof Integer) {
                    return setShowIcon((int)value);
                }
                break;
            case IMAGE_SOURCE:
                if(value instanceof Integer) {
                    return setImageSource((int)value);
                }
                break;
            case IMAGE_POSITION:
                if(value instanceof Integer) {
                    return setImagePosition((int)value);
                }
                break;
            case IMAGE:
                if(value instanceof Map) {
                    return setImage((Map<?, ?>)value);
                }
                break;
        }
        
        return super.attr(attribute, value);
    }
    
    @Override
    public void delete() {
        for(EObjectProxy rel : inRels()) {
            rel.delete();
        }

        for(EObjectProxy rel : outRels()) {
            rel.delete();
        }
        
        for(EObjectProxy child : children()) {
            child.delete();
        }

        if(getEObject().eContainer() != null) {
            CommandHandler.executeCommand(new DeleteDiagramModelObjectCommand(getEObject()));
        }
    }
    
}
