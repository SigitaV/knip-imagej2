/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2013
 *  University of Konstanz, Germany and
 *  KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * --------------------------------------------------------------------- *
 *
 */
package org.knime.knip.imagej2.core.node;

import java.util.LinkedList;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponent;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.knip.imagej2.core.IJGateway;
import org.knime.knip.imagej2.core.adapter.DataValueConfigGuiInfos;
import org.knime.knip.imagej2.core.adapter.DialogComponentGroup;
import org.knime.knip.imagej2.core.adapter.DialogComponentGroup.PLACEMENT_HINT;
import org.knime.knip.imagej2.core.adapter.IJAdapterProvider;
import org.knime.knip.imagej2.core.adapter.IJAdvancedInputAdapter;
import org.knime.knip.imagej2.core.adapter.IJInputAdapter;
import org.knime.knip.imagej2.core.adapter.ModuleItemDataValueConfig;
import org.knime.knip.imagej2.core.imagejdialog.DialogComponentImageJDlg;
import org.knime.knip.imagej2.core.imagejdialog.HarvesterModuleWrapper;
import org.knime.knip.imagej2.core.imagejdialog.SettingsModelImageJDlg;
import org.scijava.module.Module;
import org.scijava.module.ModuleInfo;
import org.scijava.module.ModuleItem;
import org.scijava.module.process.ServicePreprocessor;

/**
 * Provides a basic set of methods that are common to all IJNodeDialogs: Handling of the ImageJ dialog for basic
 * parameters, creation of combo box selection dialog components for {@link ModuleItemDataValueConfig}s, creation of the
 * column binding tab.
 *
 *
 * @author <a href="mailto:dietzc85@googlemail.com">Christian Dietz</a>
 * @author <a href="mailto:horn_martin@gmx.de">Martin Horn</a>
 * @author <a href="mailto:michael.zinsmaier@googlemail.com">Michael Zinsmaier</a>
 */
public abstract class AbstractIJNodeDialog extends DefaultNodeSettingsPane {

    /*
     * The ImageJ dialog might exist or might not exist. As long as the creation of the
     * dialog resides in this class this information is not available for the node model.
     * Therefore a dummy settings model is currently created if the ImageJ dialog doesn't
     * exist / is not used. The dummy sm allows the node model to load/save/validate something
     * even if no actual ImageJ dialog is used.
     */

    private boolean m_useDialog;

    private final SettingsModelImageJDlg m_dummyModel;

    private final DialogComponentImageJDlg m_imageJDialog;

    /**
     * Creates a new empty node dialog.
     *
     * @param info
     */
    protected AbstractIJNodeDialog(final ModuleInfo info) {
        m_dummyModel = AbstractIJNodeModel.createImageJDlgModel();
        m_dummyModel.setEnabled(false);
        final Module module = IJGateway.getInstance().getModuleService().createModule(info);
        final ServicePreprocessor servicePreproc = new ServicePreprocessor();
        IJGateway.getImageJContext().inject(servicePreproc);
        servicePreproc.process(module);

        HarvesterModuleWrapper harvesterModule = new HarvesterModuleWrapper(module);

        m_imageJDialog = new DialogComponentImageJDlg(AbstractIJNodeModel.createImageJDlgModel(), harvesterModule);
        m_useDialog = false;
    }

    /**
     * @return true if a none empty (i.e. with input widgets) DialogComponent for the ImageJ dialog has been created and
     *         not already been added to the dialog.
     */
    protected boolean hasNoneEmptyFreeImageJDialog() {
        if (!m_imageJDialog.isEmpty() && !m_useDialog) {
            return true;
        }
        return false;
    }

    /**
     * adds the DialogComponent for the ImageJ dialog at the current position to the Node Dialog if a none empty dialog
     * component exists and not has been added so far.
     */
    protected void addImageJDialogIfNoneEmpty() {
        if (!m_imageJDialog.isEmpty() && !m_useDialog) {
            addDialogComponent(m_imageJDialog);
            m_useDialog = true;
        }
    }

    /**
     * Creates a {@link DialogComponentGroup} that contains one column selection combo box for each required DataValue.
     * This is a convenient method for standard handling of {@link ModuleItemDataValueConfig}s.<br>
     * <br>
     * This method should be used in combination with the
     * {@link AbstractIJNodeModel#addColumnSelectionSettingsModel(ModuleItemDataValueConfig)} method of the node model.
     *
     * @param config will be configured using column boxes for all required DataValues
     *
     * @return the resulting {@link DialogComponentGroup}
     */
    @SuppressWarnings("unchecked")
    protected DialogComponentGroup createColumnSelectionDCG(final ModuleItemDataValueConfig config) {
        final DialogComponent[] components = new DialogComponent[config.getGuiMetaInfo().length];

        int i = 0;
        for (final DataValueConfigGuiInfos guiInfo : config.getGuiMetaInfo()) {
            final SettingsModelString sms = AbstractIJNodeModel.createColumnSelectionStringSM(guiInfo.identifier);
            components[i] = new DialogComponentColumnNameSelection(sms, guiInfo.label, 0, guiInfo.inValue);
            i++;
        }

        return new DialogComponentGroup(components, PLACEMENT_HINT.HORIZ_WRAP_3);
    }

    /**
     * Creates the column binding tab for IJ dialog input parameters and adds it to the configure dialog. If the module
     * does not have IJ dialog parameters no column binding tab is created.
     *
     * @param moduleInfo
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void createColumnBindingTab(final ModuleInfo moduleInfo) {
        //get all dcgs
        final LinkedList<DialogComponentGroup> basicInputColumnBindingDCGs = new LinkedList<DialogComponentGroup>();
        for (final ModuleItem<?> item : moduleInfo.inputs()) {
            final IJInputAdapter inputAdapter = IJAdapterProvider.getInputAdapter(item.getType());
            if (inputAdapter != null) {
                if (IJGateway.isIJDialogInputType(item.getType()) && (inputAdapter instanceof IJAdvancedInputAdapter)) {
                    final DialogComponentGroup group =
                            ((IJAdvancedInputAdapter)inputAdapter).createDialogComponents(item);
                    basicInputColumnBindingDCGs.add(group);
                }
            }
        }

        //add them to the gui
        if (basicInputColumnBindingDCGs.size() > 0) {
            createNewTab("Advanced Settings");

            for (final DialogComponentGroup dcg : basicInputColumnBindingDCGs) {
                addComponents(dcg);
            }
        }
    }

    /**
     * helper method that adds the DialogComponents of the group to the dialog and follows the placement hints of the
     * group.
     *
     * @param group
     */
    protected void addComponents(final DialogComponentGroup group) {
        if (group.hasGroupBorder()) {
            createNewGroup(group.getGroupBorderText());
        }

        int componentCount = 0;
        for (final DialogComponent dc : group.getDialogComponents()) {

            if ((group.getPlacement() == PLACEMENT_HINT.HORIZONTAL)
                    || (group.getPlacement() == PLACEMENT_HINT.HORIZ_WRAP_2)
                    || (group.getPlacement() == PLACEMENT_HINT.HORIZ_WRAP_3)) {
                setHorizontalPlacement(true);
            }

            addDialogComponent(dc);
            componentCount++;

            if ((group.getPlacement() == PLACEMENT_HINT.HORIZ_WRAP_2) && ((componentCount % 2) == 0)) {
                setHorizontalPlacement(false);
            }
            if ((group.getPlacement() == PLACEMENT_HINT.HORIZ_WRAP_3) && ((componentCount % 3) == 0)) {
                setHorizontalPlacement(false);
            }
        }

        if (group.hasGroupBorder()) {
            closeCurrentGroup();
        }
        setHorizontalPlacement(false); // back to default
    }

    //following methods are only needed to manage the dummy settings object for imageJ dialogs if no ImageJ dialog exists

    @Override
    public void loadAdditionalSettingsFrom(final NodeSettingsRO settings, final DataTableSpec[] specs)
            throws NotConfigurableException {
        super.loadAdditionalSettingsFrom(settings, specs);

        if (!m_useDialog) { //add dummy model to the settings
            try {
                m_dummyModel.loadSettingsFrom(settings);
            } catch (final InvalidSettingsException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void saveAdditionalSettingsTo(final NodeSettingsWO settings) throws InvalidSettingsException {
        super.saveAdditionalSettingsTo(settings);

        if (!m_useDialog) { //add dummy model to the settings
            m_dummyModel.saveSettingsTo(settings);
        }
    }

}
