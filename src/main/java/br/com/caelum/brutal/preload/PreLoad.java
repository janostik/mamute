package br.com.caelum.brutal.preload;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import br.com.caelum.brutal.components.RecentTagsContainer;
import br.com.caelum.brutal.migration.MigrationRunner;

@ApplicationScoped
public class PreLoad {
	@Inject private MigrationRunner migrations;
	@Inject private RecentTagsContainer tagsContainer;
	
	@PostConstruct 
	public void execute() {
		migrations.execute();
		tagsContainer.execute();
	}
	
	@PreDestroy
	public void destroy(){
		tagsContainer.destroy();
	}
	
	
}
