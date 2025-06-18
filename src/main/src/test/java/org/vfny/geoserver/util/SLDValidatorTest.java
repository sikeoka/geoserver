/* (c) 2014 Open Source Geospatial Foundation - all rights reserved
 * (c) 2001 - 2013 OpenPlans
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.vfny.geoserver.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;
import org.geoserver.platform.resource.Paths;
import org.geotools.util.logging.Logging;
import org.junit.Test;

public class SLDValidatorTest {

    static final Logger LOGGER = Logging.getLogger(SLDValidatorTest.class);

    @Test
    public void testValid() throws Exception {
        File file1 = new File("\\\\ad.local\\gdit\\DEF-NMC\\C2EDS_NITES-Next\\Projects\\C2EDS\\");
        System.err.println(file1.getCanonicalPath());
        System.err.println(file1.getPath() + " " + file1.isAbsolute() + " " + file1.exists());
        File file2 = new File(file1.getPath().replace('\\', '/'));
        System.err.println(file1.getPath().replace('\\', '/'));
        System.err.println(file2.getPath() + " " + file2.isAbsolute() + " " + file2.exists());
        File file3 = new File("/" + Paths.path(Paths.convert(file1.getPath())));
        System.err.println("/" + Paths.path(Paths.convert(file1.getPath())));
        System.err.println(file3.getPath() + " " + file3.isAbsolute() + " " + file3.exists());
        //        SLDValidator validator = new SLDValidator();
        //        List errors = validator.validateSLD(getClass().getResourceAsStream("valid.sld"));
        //
        //        // showErrors(errors);
        //        assertTrue(errors.isEmpty());
    }

    @Test
    public void testInvalid() throws Exception {
        SLDValidator validator = new SLDValidator();
        List errors = validator.validateSLD(getClass().getResourceAsStream("invalid.sld"));

        // showErrors(errors);
        assertFalse(errors.isEmpty());
    }

    /** Tests validation for Localized tag. See GEOS-9132. */
    @Test
    public void testi18nValid() throws Exception {
        SLDValidator validator = new SLDValidator();
        List errors = validator.validateSLD(getClass().getResourceAsStream("validi18n.sld"));
        assertTrue(errors.isEmpty());
    }

    void showErrors(List<Exception> errors) {
        for (Exception err : errors) {
            LOGGER.warning(err.getMessage());
        }
    }
}
