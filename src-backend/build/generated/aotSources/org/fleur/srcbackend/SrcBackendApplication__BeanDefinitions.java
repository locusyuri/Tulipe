package org.fleur.srcbackend;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ConfigurationClassUtils;

/**
 * Bean definitions for {@link SrcBackendApplication}.
 */
@Generated
public class SrcBackendApplication__BeanDefinitions {
  /**
   * Get the bean definition for 'srcBackendApplication'.
   */
  public static BeanDefinition getSrcBackendApplicationBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(SrcBackendApplication.class);
    beanDefinition.setTargetType(SrcBackendApplication.class);
    ConfigurationClassUtils.initializeConfigurationClass(SrcBackendApplication.class);
    beanDefinition.setInstanceSupplier(SrcBackendApplication$$SpringCGLIB$$0::new);
    return beanDefinition;
  }
}
