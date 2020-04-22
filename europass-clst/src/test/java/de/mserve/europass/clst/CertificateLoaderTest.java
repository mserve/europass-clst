package de.mserve.europass.clst;

import static org.junit.Assume.assumeNotNull;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.util.List;

import de.mserve.europass.model.CertificateEntry;
import de.mserve.europass.service.CertificateLoader;
import eu.europa.esig.dss.enumerations.QCStatement;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class CertificateLoaderTest extends TestCase {
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public CertificateLoaderTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(CertificateLoaderTest.class);
    }

    /**
     * Rigourous Test :-)
     */
    public void testEnmueration() {
        File f = null;
        try {
            f = new File(CertificateLoaderTest.class.getResource("e-seal.p12").toURI());
        } catch (Exception e) {
        }
        assertNotNull(f);
        CertificateLoader cl = CertificateLoader.PKCS12(f, "seal".toCharArray());
        assertNotNull(cl);
        List<CertificateEntry> lce = cl.enumerateKeys();
        assumeNotNull(lce);
        assumeTrue(lce.size() == 1);
        CertificateEntry e = lce.get(0);
        assumeNotNull("Certificate entry should not be null", e);
        assumeTrue("Check label", e.getLabel().equals(
                "Homer Simpson Vocational College (420042) [Qualified] [Qualified Signature Creation Device] [E-Seal]"));
        assertTrue("hasQC_COMPLIANCE", e.hasProperty(QCStatement.QC_COMPLIANCE));
        assertTrue("hasQC_SSCD", e.hasProperty(QCStatement.QC_SSCD));
        assertTrue("hasQCT_ESEAL", e.hasProperty(QCStatement.QCT_ESEAL));
        assertFalse("hasNoQCT_ESIGN", e.hasProperty(QCStatement.QCT_ESIGN));
    }

}
