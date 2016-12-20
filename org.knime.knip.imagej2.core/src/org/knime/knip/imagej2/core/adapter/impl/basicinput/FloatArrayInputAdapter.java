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
package org.knime.knip.imagej2.core.adapter.impl.basicinput;

import org.knime.core.data.DataRow;
import org.knime.core.data.DataValue;
import org.knime.core.data.vector.doublevector.DoubleVectorValue;
import org.scijava.module.Module;
import org.scijava.module.ModuleItem;

/**
 * Configures a ModuleItem of ImageJ type float[] with values from a user selected {@link DoubleVectorValue} compatible
 * column.
 *
 *
 * @author <a href="mailto:dietzc85@googlemail.com">Christian Dietz</a>
 */
public class FloatArrayInputAdapter extends AbstractBasicInputAdapter<float[]> {

    @Override
    public Class<float[]> getIJType() {
        return float[].class;
    }

    @Override
    protected String getSettingsNameInfix() {
        return "Float Array";
    }

    @Override
    @SuppressWarnings("rawtypes")
    protected void configModuleItem(final Module module, final DataRow row, final ModuleItem item,
                                    final int columnIndex) {
        final DoubleVectorValue value = ((DoubleVectorValue)row.getCell(columnIndex));
        float[] array = new float[value.getLength()];

        for (int d = 0; d < array.length; d++) {
            array[d] = (float)value.getValue(d);
        }

        module.setInput(item.getName(), array);
    }

    @Override
    public Class<? extends DataValue> getDataValueClass() {
        return DoubleVectorValue.class;
    }
}
