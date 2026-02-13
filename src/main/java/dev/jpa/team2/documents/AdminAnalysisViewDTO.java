package dev.jpa.team2.documents;

import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class AdminAnalysisViewDTO {
	Long user_id;
	Analysis analysis;
	Report report;
	List<Contract> contractList;
	List<Registry> registryList;
	public AdminAnalysisViewDTO(Long user_id, Analysis analysis, Report report,
			List<Contract> contractList,List<Registry> registryList)
	{
		this.user_id=user_id;
		this.analysis=analysis;
		this.report=report;
		this.contractList=contractList;
		this.registryList=registryList;
	}
}
