/* (c) 2018 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.geofence.config;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.BooleanUtils;
import org.geoserver.geofence.GeofenceAccessManager;
import org.geoserver.geofence.cache.CacheConfiguration;
import org.geoserver.platform.resource.Resource;
import org.geotools.util.logging.Logging;
import org.springframework.beans.factory.InitializingBean;

/** @author ETj (etj at geo-solutions.it) */
public class GeoFenceConfigurationManager implements InitializingBean {

    private static final Logger LOGGER = Logging.getLogger(GeofenceAccessManager.class);

    private GeoFencePropertyPlaceholderConfigurer configurer;

    private GeoFenceConfiguration geofenceConfiguration;

    private CacheConfiguration cacheConfiguration;

    private static final String PROP_INSTANCE_NAME = "instanceName";
    private static final String PROP_SERVICES_URL = "servicesUrl";
    private static final String PROP_ALLOW_REMOTE = "allowRemoteAndInlineLayers";
    private static final String PROP_GRANT_WRITE = "grantWriteToWorkspacesToAuthenticatedUsers";
    private static final String PROP_USE_ROLES = "useRolesToFilter";
    private static final String PROP_ACCEPTED_ROLES = "acceptedRoles";
    private static final String PROP_GWCCONTEXTSUFFIX = "gwc.context.suffix";
    private static final String PROP_ORGGEOSERVERREST = "org.geoserver.rest.DefaultUserGroupServiceName";
    private static final String PROP_CACHE_EXPIRE = "cacheExpire";
    private static final String PROP_CACHE_REFRESH = "cacheRefresh";
    private static final String PROP_CACHE_SIZE = "cacheSize";

    private static final String[] ALL_GEOFENCE_PROPS = {
        PROP_INSTANCE_NAME,
        PROP_SERVICES_URL,
        PROP_ALLOW_REMOTE,
        PROP_GRANT_WRITE,
        PROP_USE_ROLES,
        PROP_ACCEPTED_ROLES,
        PROP_GWCCONTEXTSUFFIX,
        PROP_ORGGEOSERVERREST,
    };
    private static final String[] ALL_CACHE_PROPS = {
        PROP_CACHE_SIZE, PROP_CACHE_REFRESH, PROP_CACHE_EXPIRE,
    };

    public GeoFenceConfiguration getConfiguration() {
        return geofenceConfiguration;
    }

    /** Updates the configuration. */
    public void setConfiguration(GeoFenceConfiguration cfg) {

        this.geofenceConfiguration = cfg;

        LOGGER.log(Level.INFO, "GeoFence configuration: instance name is {0}", cfg.getInstanceName());
    }

    public CacheConfiguration getCacheConfiguration() {
        return cacheConfiguration;
    }

    public void setCacheConfiguration(CacheConfiguration cacheConfiguration) {
        this.cacheConfiguration = cacheConfiguration;
    }

    public void storeConfiguration() throws IOException {
        Resource configurationFile = configurer.getConfigFile();

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(configurationFile.out()))) {
            writer.write("### GeoFence Module configuration file\n");
            writer.write("### \n");
            writer.write("### GeoServer will read this file at boot time.\n");
            writer.write(
                    "### This file may be automatically regenerated by GeoServer, so any changes beside the property values may be lost.\n\n");

            saveConfiguration(writer, geofenceConfiguration);
            saveConfiguration(writer, cacheConfiguration);
        }
    }

    /** Saves current configuration to disk. */
    protected void saveConfiguration(Writer writer, GeoFenceConfiguration cfg) throws IOException {

        writer.write("### GeoFence main configuration\n\n");
        Properties props = configAsProperties(cfg);
        for (String propname : ALL_GEOFENCE_PROPS) {
            saveConfig(writer, propname, props.getProperty(propname));
        }
    }

    public void saveConfiguration(Writer writer, CacheConfiguration cfg) throws IOException {

        writer.write("\n\n### Cache configuration\n\n");
        Properties props = configAsProperties(cfg);
        for (String propname : ALL_CACHE_PROPS) {
            saveConfig(writer, propname, props.getProperty(propname));
        }
    }

    protected void saveConfig(Writer writer, String name, Object value) throws IOException {
        writer.write(name + "=" + String.valueOf(value) + "\n");
    }

    /** Returns a copy of the configuration. */
    public void setConfigurer(GeoFencePropertyPlaceholderConfigurer configurer) {
        this.configurer = configurer;
    }

    /**
     * The PropertyPlaceholderConfigurer may have race conditions sometimes, so we're going to read the props from the
     * file and assign them to the GeoFenceConfiguration
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        LOGGER.log(
                Level.INFO,
                "GeoFence configuration: force setting properties from {0}",
                configurer.getConfigFile().path());

        if (loadConfiguration())
            LOGGER.log(
                    Level.INFO,
                    "GeoFence configuration: instance name is {0}",
                    geofenceConfiguration.getInstanceName());
    }

    public boolean loadConfiguration() throws IOException {
        Resource configurationFile = configurer.getConfigFile();

        Properties defaults = configAsProperties(geofenceConfiguration);
        defaults.putAll(configAsProperties(cacheConfiguration));

        Properties props = new Properties(defaults);

        try {
            props.load(configurationFile.in());
        } catch (IllegalStateException e) {

            LOGGER.log(
                    Level.INFO,
                    "GeoFence configuration: could not open property file {0}",
                    configurer.getConfigFile().path());
            return false;
        }

        loadConfiguration(props, geofenceConfiguration);
        loadConfiguration(props, cacheConfiguration);
        return true;
    }

    private void loadConfiguration(Properties props, GeoFenceConfiguration cfg) {
        cfg.setInstanceName(props.getProperty(PROP_INSTANCE_NAME));
        cfg.setServicesUrl(props.getProperty(PROP_SERVICES_URL));
        cfg.setAllowRemoteAndInlineLayers(BooleanUtils.toBoolean(props.getProperty(PROP_ALLOW_REMOTE)));
        cfg.setGrantWriteToWorkspacesToAuthenticatedUsers(BooleanUtils.toBoolean(props.getProperty(PROP_GRANT_WRITE)));
        cfg.setUseRolesToFilter(BooleanUtils.toBoolean(props.getProperty(PROP_USE_ROLES)));
        cfg.setAcceptedRoles(props.getProperty(PROP_ACCEPTED_ROLES));
        cfg.setGwcContextSuffix(props.getProperty(PROP_GWCCONTEXTSUFFIX));
    }

    private void loadConfiguration(Properties props, CacheConfiguration cfg) {
        cfg.setSize(Long.parseLong(props.getProperty(PROP_CACHE_SIZE)));
        cfg.setRefreshMilliSec(Long.parseLong(props.getProperty(PROP_CACHE_REFRESH)));
        cfg.setExpireMilliSec(Long.parseLong(props.getProperty(PROP_CACHE_EXPIRE)));
    }

    public Properties configAsProperties(GeoFenceConfiguration cfg) {
        Properties props = new Properties(10);
        props.setProperty(PROP_INSTANCE_NAME, cfg.getInstanceName());
        props.setProperty(PROP_SERVICES_URL, cfg.getServicesUrl());
        props.setProperty(PROP_ALLOW_REMOTE, format_prop(cfg.isAllowRemoteAndInlineLayers()));
        props.setProperty(PROP_GRANT_WRITE, format_prop(cfg.isGrantWriteToWorkspacesToAuthenticatedUsers()));
        props.setProperty(PROP_USE_ROLES, format_prop(cfg.isUseRolesToFilter()));
        props.setProperty(PROP_ACCEPTED_ROLES, format_prop(cfg.getAcceptedRoles()));
        props.setProperty(PROP_GWCCONTEXTSUFFIX, format_prop(cfg.getGwcContextSuffix()));
        props.setProperty(PROP_ORGGEOSERVERREST, format_prop(cfg.getDefaultUserGroupServiceName()));
        return props;
    }

    public Properties configAsProperties(CacheConfiguration cfg) {
        Properties props = new Properties(3);
        props.setProperty(PROP_CACHE_SIZE, String.valueOf(cfg.getSize()));
        props.setProperty(PROP_CACHE_REFRESH, String.valueOf(cfg.getRefreshMilliSec()));
        props.setProperty(PROP_CACHE_EXPIRE, String.valueOf(cfg.getExpireMilliSec()));
        return props;
    }

    static String format_prop(String s) {
        return s != null ? s : "";
    }

    static String format_prop(boolean b) {
        return String.valueOf(b);
    }
}
