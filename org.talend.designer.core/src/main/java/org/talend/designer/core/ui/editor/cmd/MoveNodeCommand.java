// ============================================================================
//
// Copyright (C) 2006-2007 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.core.ui.editor.cmd;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.commands.Command;
import org.eclipse.ui.PlatformUI;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.AbstractMultiPageTalendEditor;
import org.talend.designer.core.ui.editor.nodes.Node;

/**
 * Move a given node to another location. <br/>
 * 
 * $Id$
 * 
 */
public class MoveNodeCommand extends Command {

    private Node node;

    private Point newPos;

    private Point oldPos;

    private List<Node> currentSelection;

    /**
     * Move the given node to another location.
     * 
     * @param node
     * @param newPos
     */
    public MoveNodeCommand(Node node, Point newPos) {
        this.node = node;
        this.newPos = newPos;
        setLabel(Messages.getString("MoveNodeCommand.Label")); //$NON-NLS-1$

        initializeCurrentSelection();
    }

    @SuppressWarnings("unchecked")//$NON-NLS-1$
    @Override
    public boolean canExecute() {
        Rectangle movingRect = new Rectangle(newPos, node.getSize());
        for (Node currentNode : (List<Node>) node.getProcess().getGraphicalNodes()) {
            Rectangle currentRect = new Rectangle(currentNode.getLocation(), currentNode.getSize());
            if ((currentRect.intersects(movingRect)) && (!currentSelection.contains(currentNode))) {
                return false;
            }
        }
        return true;
    }

    private void initializeCurrentSelection() {
        currentSelection = new ArrayList<Node>();
        AbstractMultiPageTalendEditor multiPageTalendEditor = (AbstractMultiPageTalendEditor) PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow().getActivePage().getActiveEditor();
        GraphicalViewer viewer = multiPageTalendEditor.getTalendEditor().getViewer();
        for (EditPart editPart : (List<EditPart>) viewer.getSelectedEditParts()) {
            if (editPart.getModel() instanceof Node) {
                currentSelection.add((Node) editPart.getModel());
            }
        }
    }

    public void execute() {
        oldPos = this.node.getLocation();
        this.node.setLocation(newPos);
    }

    public void undo() {
        this.node.setLocation(oldPos);
    }

    public void redo() {
        this.node.setLocation(newPos);
    }
}
