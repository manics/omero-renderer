/*
 * omeis.providers.re.QuantumManager
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package omeis.providers.re;

// Java imports
import java.util.Iterator;
import java.util.List;

// Third-party libraries

// Application-internal dependencies
import ome.api.IPixels;
import ome.model.core.Channel;
import ome.model.core.Pixels;
import ome.model.display.ChannelBinding;
import ome.model.display.QuantumDef;
import ome.model.enums.PixelsType;
import omeis.providers.re.quantum.QuantumFactory;
import omeis.providers.re.quantum.QuantumStrategy;

/**
 * Manages the strategy objects for each wavelength.
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author <br>
 *         Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:a.falconi@dundee.ac.uk"> a.falconi@dundee.ac.uk</a>
 * @version 2.2 <small> (<b>Internal version:</b> $Revision$ $Date:
 *          2005/06/10 17:36:31 $) </small>
 * @since OME2.2
 */
class QuantumManager {

    /** The pixels metadata. */
    private Pixels metadata;

    /**
     * Contains a strategy object for each wavelength. Indexed according to the
     * wavelength indexes in the <i>OME</i> 5D pixels file.
     */
    private QuantumStrategy[] wavesStg;

    /** Omero Pixels service */
    private IPixels iPixels;

    /**
     * Creates a new instance.
     * 
     * @param metadata
     *            The pixels metadata.
     */
    QuantumManager(Pixels metadata, IPixels iPixels) {
        this.iPixels = iPixels;
        this.metadata = metadata;
        wavesStg = new QuantumStrategy[metadata.getSizeC().intValue()];
    }

    /**
     * Creates and configures an appropriate strategy for each wavelength. The
     * previous window interval settings of each wavelength are retained by the
     * new strategy.
     * 
     * @param qd
     *            The quantum definition which dictates what strategy to use.
     * @param type
     *            The pixels' type.
     * @param waves
     *            Rendering settings associated to each wavelength (channel).
     */
    void initStrategies(QuantumDef qd, PixelsType type,
            List<ChannelBinding> waves) {
        ChannelBinding[] cb = waves.toArray(new ChannelBinding[waves.size()]);
        initStrategies(qd, type, cb);
    }

    /**
     * Creates and configures an appropriate strategy for each wavelength. The
     * previous window interval settings of each wavelength are retained by the
     * new strategy.
     * 
     * @param qd
     *            The quantum definition which dictates what strategy to use.
     * @param type
     *            The pixels' type.
     * @param waves
     *            Rendering settings associated to each wavelength (channel).
     */
    void initStrategies(QuantumDef qd, PixelsType type, ChannelBinding[] waves) {
        QuantumStrategy stg;
        double gMin, gMax;
        List channels = this.metadata.getChannels();
        int w = 0;
        Channel channel;
        for (Iterator i = channels.iterator(); i.hasNext();) {
            channel = (Channel) i.next();
            stg = QuantumFactory.getStrategy(qd, type, iPixels);
            gMin = channel.getStatsInfo().getGlobalMin().doubleValue();
            gMax = channel.getStatsInfo().getGlobalMax().doubleValue();
            stg.setExtent(gMin, gMax);
            stg
                    .setMapping(waves[w].getFamily(), waves[w].getCoefficient()
                            .doubleValue(), waves[w].getNoiseReduction()
                            .booleanValue());
            stg.setWindow(waves[w].getInputStart(), waves[w].getInputEnd());

            wavesStg[w] = stg;
            w++;
        }
    }

    /**
     * Retrieves the configured strategy for the specified wavelength.
     * 
     * @param w
     *            The wavelength index in the <i>OME</i> 5D-pixels file.
     * @return See above.
     */
    QuantumStrategy getStrategyFor(int w) {
        return wavesStg[w];
    }

}
