// ============================================================================
//
// Talend Community Edition
//
// Copyright (C) 2006 Talend - www.talend.com
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
//
// ============================================================================
package org.talend.designer.mapper.ui.visualmap.table;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.talend.commons.ui.image.EImage;
import org.talend.commons.ui.image.ImageProvider;
import org.talend.commons.ui.swt.advanced.dataeditor.commands.ExtendedTableRemoveCommand;
import org.talend.commons.ui.swt.extended.table.AbstractExtendedTableViewer;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreator;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreatorColumn;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreator.LAYOUT_MODE;
import org.talend.commons.ui.swt.tableviewer.behavior.DefaultTableLabelProvider;
import org.talend.commons.ui.swt.tableviewer.behavior.ITableCellValueModifiedListener;
import org.talend.commons.ui.swt.tableviewer.behavior.TableCellValueModifiedEvent;
import org.talend.commons.ui.swt.tableviewer.tableeditor.ButtonPushImageTableEditorContent;
import org.talend.commons.ui.ws.WindowSystem;
import org.talend.commons.utils.data.bean.IBeanPropertyAccessors;
import org.talend.designer.mapper.managers.MapperManager;
import org.talend.designer.mapper.managers.UIManager;
import org.talend.designer.mapper.model.table.AbstractDataMapTable;
import org.talend.designer.mapper.model.table.OutputTable;
import org.talend.designer.mapper.model.tableentry.FilterTableEntry;
import org.talend.designer.mapper.model.tableentry.ITableEntry;
import org.talend.designer.mapper.model.tableentry.OutputColumnTableEntry;
import org.talend.designer.mapper.ui.dnd.DragNDrop;
import org.talend.designer.mapper.ui.proposal.expression.ExpressionProposal;
import org.talend.designer.mapper.ui.visualmap.zone.Zone;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 * 
 * $Id$
 * 
 */
public class OutputDataMapTableView extends DataMapTableView {

    protected ExpressionProposal proposal;

    public OutputDataMapTableView(Composite parent, int style, AbstractDataMapTable abstractDataMapTable, MapperManager mapperManager) {
        super(parent, style, abstractDataMapTable, mapperManager);
    }

    @Override
    public void initColumnsOfTableColumns(final TableViewerCreator tableViewerCreatorForColumns) {
        TableViewerCreatorColumn column = new TableViewerCreatorColumn(tableViewerCreatorForColumns);
        column.setTitle("Expression");
        column.setId(DataMapTableView.ID_EXPRESSION_COLUMN);
        column.setBeanPropertyAccessors(new IBeanPropertyAccessors<OutputColumnTableEntry, String>() {

            public String get(OutputColumnTableEntry bean) {
                return bean.getExpression();
            }

            public void set(OutputColumnTableEntry bean, String value) {
                bean.setExpression(value);
                tableViewerCreatorForColumns.getTableViewer().refresh(bean);
            }

        });
        column.setModifiable(true);
        column.setDefaultInternalValue("");
        createExpressionCellEditor(tableViewerCreatorForColumns, column, new Zone[] { Zone.INPUTS, Zone.VARS }, false);
        column.setWeight(COLUMN_EXPRESSION_SIZE_WEIGHT);

        column = new TableViewerCreatorColumn(tableViewerCreatorForColumns);
        column.setTitle(DataMapTableView.COLUMN_NAME);
        column.setId(DataMapTableView.ID_NAME_COLUMN);
        column.setBeanPropertyAccessors(new IBeanPropertyAccessors<OutputColumnTableEntry, String>() {

            public String get(OutputColumnTableEntry bean) {
                return bean.getMetadataColumn().getLabel();
            }

            public void set(OutputColumnTableEntry bean, String value) {
                bean.getMetadataColumn().setLabel(value);
            }

        });
        column.setWeight(COLUMN_NAME_SIZE_WEIGHT);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.mapper.ui.visualmap.table.DataMapTableView#initTableConstraints()
     */
    @Override
    protected void initTableFilters() {
        createFiltersTable();

        List<FilterTableEntry> entries = ((OutputTable) getDataMapTable()).getFilterEntries();

        // correct partially layout problem with GTK when cell editor value is applied
        tableViewerCreatorForFilters.setAdjustWidthValue(WindowSystem.isGTK() ? -20 : ADJUST_WIDTH_VALUE);

        tableViewerCreatorForFilters.init(entries);
        updateGridDataHeightForTableConstraints();

        if (WindowSystem.isGTK()) {
            tableViewerCreatorForFilters.layout();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.mapper.ui.visualmap.table.DataMapTableView#addEntriesActionsComponents()
     */
    @Override
    protected boolean addToolItems() {
        createFiltersToolItems();
        // addToolItemSeparator();
        // createToolItems();
        return true;
    }

    protected void createFiltersTable() {

        this.extendedTableViewerForFilters = new AbstractExtendedTableViewer<FilterTableEntry>(((OutputTable) abstractDataMapTable)
                .getTableFiltersEntriesModel(), centerComposite) {

            @Override
            protected void createColumns(TableViewerCreator<FilterTableEntry> tableViewerCreator, Table table) {
                createFiltersColumns(tableViewerCreator);
            }

            /*
             * (non-Javadoc)
             * 
             * @see org.talend.commons.ui.swt.extended.macrotable.AbstractExtendedTableViewer#setTableViewerCreatorOptions(org.talend.commons.ui.swt.tableviewer.TableViewerCreator)
             */
            @Override
            protected void setTableViewerCreatorOptions(TableViewerCreator<FilterTableEntry> newTableViewerCreator) {
                super.setTableViewerCreatorOptions(newTableViewerCreator);
                newTableViewerCreator.setColumnsResizableByDefault(false);
                newTableViewerCreator.setBorderVisible(false);
                newTableViewerCreator.setLayoutMode(LAYOUT_MODE.FILL_HORIZONTAL);
                // tableViewerCreatorForColumns.setUseCustomItemColoring(this.getDataMapTable() instanceof
                // AbstractInOutTable);
                newTableViewerCreator.setFirstColumnMasked(true);

            }

        };
        tableViewerCreatorForFilters = this.extendedTableViewerForFilters.getTableViewerCreator();
        this.extendedTableViewerForFilters.setCommandStack(mapperManager.getCommandStack());

        tableForConstraints = tableViewerCreatorForFilters.getTable();
        tableForConstraintsGridData = new GridData(GridData.FILL_HORIZONTAL);
        tableForConstraints.setLayoutData(tableForConstraintsGridData);

        boolean tableConstraintsVisible = false;
        if (abstractDataMapTable instanceof OutputTable) {
            tableConstraintsVisible = ((OutputTable) abstractDataMapTable).getFilterEntries().size() > 0;
        }

        tableForConstraintsGridData.exclude = !tableConstraintsVisible;
        tableForConstraints.setVisible(tableConstraintsVisible);

        new DragNDrop(mapperManager, tableForConstraints, false, true);

        tableViewerCreatorForFilters.addCellValueModifiedListener(new ITableCellValueModifiedListener() {

            public void cellValueModified(TableCellValueModifiedEvent e) {
                unselectAllEntriesIfErrorDetected(e);
            }
        });

        final TableViewer tableViewer = tableViewerCreatorForFilters.getTableViewer();

        tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            public void selectionChanged(SelectionChangedEvent event) {
                selectThisDataMapTableView();
                UIManager uiManager = mapperManager.getUiManager();
                uiManager.processSelectedDataMapEntries(OutputDataMapTableView.this, uiManager.extractSelectedTableEntries(tableViewer
                        .getSelection()), true, false);
            }

        });

        tableForConstraints.addListener(SWT.KeyDown, new Listener() {

            public void handleEvent(Event event) {
                processEnterKeyDown(tableViewerCreatorForFilters, event);
            }

        });

        tableViewerCreatorForFilters.setLabelProvider(new DefaultTableLabelProvider(tableViewerCreatorForFilters) {

            @Override
            public Color getBackground(Object element, int columnIndex) {
                return getBackgroundCellColor(tableViewerCreator, element, columnIndex);
            }

            @Override
            public Color getForeground(Object element, int columnIndex) {
                return getForegroundCellColor(tableViewerCreator, element, columnIndex);
            }

        });

        initShowMessageErrorListener(tableForConstraints);
    }

    public void createFiltersColumns(final TableViewerCreator<FilterTableEntry> tableViewerCreatorForFilters) {
        TableViewerCreatorColumn column = new TableViewerCreatorColumn(tableViewerCreatorForFilters);
        column.setTitle("Filters conditions (AND)");
        column.setId(DataMapTableView.ID_EXPRESSION_COLUMN);
        column.setBeanPropertyAccessors(new IBeanPropertyAccessors<FilterTableEntry, String>() {

            public String get(FilterTableEntry bean) {
                return bean.getExpression();
            }

            public void set(FilterTableEntry bean, String value) {
                bean.setExpression(value);
            }

        });
        column.setModifiable(true);
        column.setDefaultInternalValue("");
        createExpressionCellEditor(tableViewerCreatorForFilters, column, new Zone[] { Zone.INPUTS, Zone.VARS }, true);
        column.setWeight(99);
        column.setMoveable(false);
        column.setResizable(false);

        // Column with remove button
        column = new TableViewerCreatorColumn(tableViewerCreatorForFilters);
        column.setTitle("");
        column.setDefaultDisplayedValue("");
        column.setWidth(16);
        column.setMoveable(false);
        column.setResizable(false);
        ButtonPushImageTableEditorContent buttonImage = new ButtonPushImageTableEditorContent() {

            /*
             * (non-Javadoc)
             * 
             * @see org.talend.commons.ui.swt.tableviewer.tableeditor.ButtonImageTableEditorContent#selectionEvent(org.talend.commons.ui.swt.tableviewer.TableViewerCreatorColumn,
             * java.lang.Object)
             */
            @Override
            protected void selectionEvent(TableViewerCreatorColumn column, Object bean) {
                ExtendedTableRemoveCommand removeCommand = new ExtendedTableRemoveCommand(bean, extendedTableViewerForFilters
                        .getExtendedTableModel());
                mapperManager.removeTableEntry((ITableEntry) bean);
                mapperManager.executeCommand(removeCommand);
                tableViewerCreatorForFilters.getTableViewer().refresh();
                List list = tableViewerCreatorForFilters.getInputList();
                updateGridDataHeightForTableConstraints();
                if (list != null && list.size() == 0) {
                    showTableConstraints(false);
                } else {
                    showTableConstraints(true);
                }

            }

        };
        buttonImage.setImage(ImageProvider.getImage(EImage.MINUS_ICON));
        column.setTableEditorContent(buttonImage);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.mapper.ui.visualmap.table.DataMapTableView#getZone()
     */
    @Override
    public Zone getZone() {
        return Zone.OUTPUTS;
    }

    @Override
    public void unselectAllConstraintEntries() {
        tableViewerCreatorForFilters.getSelectionHelper().deselectAll();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.mapper.ui.visualmap.table.DataMapTableView#toolbarNeededToBeRightStyle()
     */
    @Override
    public boolean toolbarNeededToBeRightStyle() {
        return false;
    }

}
