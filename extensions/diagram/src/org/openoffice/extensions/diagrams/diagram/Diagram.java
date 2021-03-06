package org.openoffice.extensions.diagrams.diagram;

import com.sun.star.awt.Point;
import com.sun.star.awt.Size;
import com.sun.star.beans.Property;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.XNamed;
import com.sun.star.drawing.XDrawPage;
import com.sun.star.drawing.XShape;
import com.sun.star.drawing.XShapes;
import com.sun.star.frame.XFrame;
import com.sun.star.frame.XModel;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.IndexOutOfBoundsException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.uno.AnyConverter;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openoffice.extensions.diagrams.Controller;
import org.openoffice.extensions.diagrams.Gui;



public abstract class Diagram {


    protected   Controller              m_Controller        = null;
    protected   Gui                     m_Gui               = null;
    protected   XFrame                  m_xFrame            = null;
    protected   XModel                  m_xModel            = null;
    protected   XMultiServiceFactory    m_xMSF              = null;

    protected   XDrawPage               m_xDrawPage         = null;
    protected   int                     m_DiagramID         = -1;

    public      PageProps               m_PageProps         = null;
    //Width-Height-BorderLeft-BorderRight-BorderTop-BorderBottom

    // m_GroupSize is the side of the bigest possible cube in the draw page
    protected   int                     m_DrawAreaWidth     = 0;
    protected   int                     m_DrawAreaHeight    = 0;
    protected   XShape                  m_xGroupShape       = null;
    protected   XNamed                  m_xNamed            = null;
    protected   XShapes                 m_xShapes           = null;

    protected short                     m_Style;

    protected final int                 COLOR               = 255;

    protected final int[]               aCOLORS             = { 65280, 255, 16711680, 16776960,
                                                                9699435, 16737843, 47359, 12076800 };

    
    protected boolean         m_IsSelectAllShape;
    protected boolean         m_IsColor;
    protected boolean         m_IsBaseColors;
    protected boolean         m_IsBaseColorsWithGradients;
    protected boolean         m_IsGradients;
    protected int             m_iColor;
    protected int             m_iStartColor;
    protected int             m_iEndColor;
    protected short           m_sRounded;  //0, 1, 2
    protected short           m_sTransparency; //0, 1, 2
    protected boolean         m_IsMonographic;
    protected boolean         m_IsFrame;
    protected boolean         m_IsRoundedFrame;
    protected boolean         m_IsAction;

    public Diagram(){ }
    
    public Diagram(Controller controller, Gui gui, XFrame xFrame) {
        m_Controller    = controller;
        m_Gui           = gui;
        m_xFrame        = xFrame;
        m_xModel = m_xFrame.getController().getModel();
        m_xMSF = (XMultiServiceFactory) UnoRuntime.queryInterface(XMultiServiceFactory.class, m_xModel);
        setGroupSize();
    }

    public void setFocusGroupShape(){
        XShapes xShapes = getController().getSelectedShapes();
        getController().setSelectedShape(m_xGroupShape);
        if(getController().getSelectedShapes() == null)
            getController().setSelectedShape(xShapes);
    }

    public void setSelectedDiagramProps(boolean bool){
        m_IsSelectAllShape = bool;
    }

    public void setColorProps(boolean bool){
        m_IsColor = bool;
    }

    public void setGradientProps(boolean bool){
        m_IsGradients = bool;
    }

    public void setBaseColorsProps(boolean bool){
        m_IsBaseColors = bool;
    }

    public void setBaseColorsWithGradientsProps(boolean bool){
        m_IsBaseColorsWithGradients = bool;
    }

    public void setColorProps(int color){
        m_iColor = color;
    }

    public void setStartColorProps(int color){
        m_iStartColor = color;
    }

    public void setEndColorProps(int color){
        m_iEndColor = color;
    }

    public void setRoundedProps(short type){
        m_sRounded = type;
    }

    public void setTransparencyProps(short type){
        m_sTransparency = type;
    }

    public void setMonographicProps(boolean bool){
        m_IsMonographic = bool;
    }

    public void setFrameProps(boolean bool){
        m_IsFrame = bool;
    }

    public void setRoundedFrameProps(boolean bool){
        m_IsRoundedFrame = bool;
    }

    public void setActionProps(boolean bool){
        m_IsAction = bool;
    }

    // determinde m_GroupSize
    public final void setGroupSize(){
        if(m_xDrawPage == null);
            m_xDrawPage = getController().getCurrentPage();
        if(m_PageProps == null)
            adjustPageProps();
        if(m_PageProps != null){
            m_DrawAreaWidth = m_PageProps.Width - m_PageProps.BorderLeft - m_PageProps.BorderRight;
            m_DrawAreaHeight = m_PageProps.Height - m_PageProps.BorderTop - m_PageProps.BorderBottom;
        }
    }

    // instantiate PageProps object
    public void adjustPageProps(){
        int width           = 0;
        int height          = 0;
        int borderLeft      = 0;
        int borderRight     = 0;
        int borderTop       = 0;
        int borderBottom    = 0;
        try {
            XPropertySet xPageProperties = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, m_xDrawPage);
            width           = AnyConverter.toInt(xPageProperties.getPropertyValue("Width"));
            height          = AnyConverter.toInt(xPageProperties.getPropertyValue("Height"));
            borderLeft      = AnyConverter.toInt(xPageProperties.getPropertyValue("BorderLeft"));
            borderRight     = AnyConverter.toInt(xPageProperties.getPropertyValue("BorderRight"));
            borderTop       = AnyConverter.toInt(xPageProperties.getPropertyValue("BorderTop"));
            borderBottom    = AnyConverter.toInt(xPageProperties.getPropertyValue("BorderBottom"));

            if(borderLeft < 1000)
                borderLeft = 1000;
            if(borderRight < 1000)
                borderRight = 1000;
            if(borderTop < 1000)
                borderTop = 1000;
            if(borderBottom < 1000)
                borderBottom = 1000;

        } catch (IllegalArgumentException ex) {
            System.err.println("IllegalArgumentException in Diagram.getPagePoperties(). Message:\n" + ex.getLocalizedMessage());
        } catch (UnknownPropertyException ex) {
            System.err.println("UnknownPropertyException in Diagram.getPagePoperties(). Message:\n" + ex.getLocalizedMessage());
        } catch (WrappedTargetException ex) {
            System.err.println("WrappedTargetException in Diagram.getPagePoperties(). Message:\n" + ex.getLocalizedMessage());
        }
        m_PageProps = new PageProps( width, height, borderLeft, borderRight, borderTop, borderBottom );
    }

    // get methods

    public Controller getController(){
        return m_Controller;
    }

    public Gui getGui(){
        return m_Gui;
    }

    public abstract String getDiagramTypeName();

    public int getDiagramID(){
        return m_DiagramID;
    }

    public int getDrawAreaWidth(){
        return m_DrawAreaWidth;
    }
    
    public int getDrawAreaHeight(){
        return m_DrawAreaHeight;
    }

    // set m_xDrawPage, PageProps, m_xGroupSize, m_xGroupShape and m_xShapes
    public void createDiagram(){
        try {
            m_xDrawPage = getController().getCurrentPage();
            m_DiagramID = (int) (Math.random()*10000);

            // set diagramName in the Controller object
            String diagramName = getDiagramTypeName() + m_DiagramID;
            getController().setLastDiagramName(diagramName);

            // set new PageProps object with data of page
            // width, height, borderLeft, borderRight, borderTop, borderBottom
            adjustPageProps();

            // get minimum of width and height
            setGroupSize();

            m_xGroupShape = (XShape) UnoRuntime.queryInterface(XShape.class, m_xMSF.createInstance ("com.sun.star.drawing.GroupShape"));
            m_xNamed = (XNamed) UnoRuntime.queryInterface(XNamed.class, m_xGroupShape);
            m_xNamed.setName( getDiagramTypeName() + m_DiagramID + "-GroupShape" );
            m_xDrawPage.add(m_xGroupShape);
            m_xShapes = (XShapes) UnoRuntime.queryInterface(XShapes.class, m_xGroupShape );
        } catch (Exception ex) {
            System.err.println(ex.getLocalizedMessage());
        }
    }

    //initial members: m_xDrawPage, m_DiagramID, m_xShapes
    public void initDiagram(){
        try {
            XShape xCurrShape = null;
            String currShapeName = "";
            m_xDrawPage = getController().getCurrentPage();
            String diagramIDName = getController().getCurrentDiagramIdName();
            m_DiagramID = getController().parseInt(diagramIDName);
            for(int i=0; i < m_xDrawPage.getCount(); i++){
                xCurrShape = (XShape) UnoRuntime.queryInterface(XShape.class, m_xDrawPage.getByIndex(i));
                currShapeName = getShapeName(xCurrShape);
                if (currShapeName.contains(diagramIDName) && currShapeName.startsWith(getDiagramTypeName()) && currShapeName.endsWith("GroupShape")) {
                    m_xShapes = (XShapes) UnoRuntime.queryInterface(XShapes.class, xCurrShape );
                    m_xGroupShape = (XShape) UnoRuntime.queryInterface(XShape.class, xCurrShape );
                }
            }
        } catch (IndexOutOfBoundsException ex) {
            System.err.println(ex.getLocalizedMessage());
        } catch (WrappedTargetException ex) {
            System.err.println(ex.getLocalizedMessage());
        }
    }

    public abstract void refreshDiagram();

    public abstract void addShape();

    public abstract void removeShape();

    public abstract void refreshShapeProperties();

    public abstract void setShapeProperties(XShape xShape, String type);


    public void removeShapeFromGroup(XShape xShape){
        m_xShapes.remove(xShape);
    }

    public XShape createShape(String type, int num){
        XShape xShape = null;
        try {
            xShape = (XShape) UnoRuntime.queryInterface(XShape.class, m_xMSF.createInstance ("com.sun.star.drawing." + type ));
            XNamed xNamed = (XNamed) UnoRuntime.queryInterface(XNamed.class,xShape);
            xNamed.setName(getDiagramTypeName() + m_DiagramID + "-" + type + num);
        }  catch (Exception ex) {
            System.err.println(ex.getLocalizedMessage());
        }
        return xShape;
    }

    public void renameShapes(String oldDiagramName, String newDiagramName){
        XShape xShape = null;
        try {
            XNamed xNamed = (XNamed) UnoRuntime.queryInterface(XNamed.class, m_xGroupShape);
            String shapeName = xNamed.getName();
            shapeName = shapeName.replace(oldDiagramName, newDiagramName);
            xNamed.setName(shapeName);
            for(int i=0; i < m_xShapes.getCount(); i++){
                xShape = (XShape)UnoRuntime.queryInterface(XShape.class, m_xShapes.getByIndex(i));
                if(xShape != null){
                    xNamed = (XNamed) UnoRuntime.queryInterface(XNamed.class, xShape);
                    shapeName = xNamed.getName();
                    shapeName = shapeName.replace(oldDiagramName, newDiagramName);
                    xNamed.setName(shapeName);
                }
            }
        } catch (IndexOutOfBoundsException ex) {
            System.out.println(ex.getLocalizedMessage());
        } catch (WrappedTargetException ex) {
            System.out.println(ex.getLocalizedMessage());
        }
    }

    public XShape createShape(String type, int num, int width, int height){
        XShape xShape = null;
        try {
            xShape = createShape(type, num);
            xShape.setSize(new Size(width, height));
        }  catch (Exception ex) {
            System.err.println(ex.getLocalizedMessage());
        }
        return xShape;
    }

    public XShape createShape(String type, int num, int x, int y, int width, int height){
        XShape xShape = createShape(type, num, width, height);
        xShape.setPosition(new Point(x, y));
        return xShape;
    }
/*
    public void setColorOfShape(XShape xShape, short num){
        setColorOfShape(xShape, aCOLORS[(num - 1) % 8]);
        if(getGui() != null && getGui().getControlDialogWindow() != null)
            getGui().setImageColorOfControlDialog(aCOLORS[ num % 8] );
    }
*/
    public void setColorOfShape(XShape xShape, int color){
        try {
            XPropertySet xProp = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, xShape);
            xProp.setPropertyValue("FillColor", new Integer(color));
        }  catch (Exception ex) {
            System.err.println(ex.getLocalizedMessage());
        }
    }

    public String getShapeName(XShape xShape){
        if(xShape != null){
           XNamed xNamed = (XNamed) UnoRuntime.queryInterface(XNamed.class,xShape);
           return xNamed.getName();
        }
        return null;
    }

    public void setChangedMode(short selected) {
        m_Style = selected;
    }

    public void test(Object obj){
            XPropertySet xProp = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, obj);
            Property[] props = xProp.getPropertySetInfo().getProperties();
            for (Property p : props)
                System.out.println(p.Name + " "  + p.Type.getTypeName());
    }
 
}
