/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.trino.plugin.singlestore;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.singlestore.jdbc.Driver;
import io.trino.plugin.jdbc.BaseJdbcConfig;
import io.trino.plugin.jdbc.ConnectionFactory;
import io.trino.plugin.jdbc.DecimalModule;
import io.trino.plugin.jdbc.DriverConnectionFactory;
import io.trino.plugin.jdbc.ForBaseJdbc;
import io.trino.plugin.jdbc.JdbcClient;
import io.trino.plugin.jdbc.credential.CredentialProvider;

import java.util.Properties;

import static io.airlift.configuration.ConfigBinder.configBinder;

public class SingleStoreClientModule
        implements Module
{
    @Override
    public void configure(Binder binder)
    {
        binder.bind(JdbcClient.class).annotatedWith(ForBaseJdbc.class).to(SingleStoreClient.class).in(Scopes.SINGLETON);
        configBinder(binder).bindConfig(SingleStoreConfig.class);
        binder.install(new DecimalModule());
    }

    @Provides
    @Singleton
    @ForBaseJdbc
    public static ConnectionFactory createConnectionFactory(BaseJdbcConfig config, CredentialProvider credentialProvider, SingleStoreConfig memsqlConfig)
    {
        Properties connectionProperties = new Properties();
        // we don't want to interpret tinyInt type (with cardinality as 2) as boolean/bit
        connectionProperties.setProperty("tinyInt1isBit", "false");
        connectionProperties.setProperty("autoReconnect", String.valueOf(memsqlConfig.isAutoReconnect()));
        connectionProperties.setProperty("connectTimeout", String.valueOf(memsqlConfig.getConnectionTimeout().toMillis()));

        return new DriverConnectionFactory(
                new Driver(),
                config.getConnectionUrl(),
                connectionProperties,
                credentialProvider);
    }
}
