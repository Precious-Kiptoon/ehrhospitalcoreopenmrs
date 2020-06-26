package org.openmrs.module.hospitalcore.impl;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang.math.NumberUtils;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.Order;
import org.openmrs.Role;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.hospitalcore.LabService;
import org.openmrs.module.hospitalcore.db.LabDAO;
import org.openmrs.module.hospitalcore.model.Lab;
import org.openmrs.module.hospitalcore.model.LabTest;

public class LabServiceImpl extends BaseOpenmrsService implements LabService {
	protected LabDAO dao;

	public void setDao(LabDAO dao) {
		this.dao = dao;
	}

	public Lab saveLab(Lab lab) throws APIException {
		return this.dao.saveLab(lab);
	}

	public List<Lab> getAllActivelab() throws APIException {
		return this.dao.getAllActivelab();
	}

	public List<Lab> getAllLab() throws APIException {
		return this.dao.getAllLab();
	}

	public Lab getLabById(Integer labId) throws APIException {
		return this.dao.getLabById(labId);
	}

	public Lab getLabByName(String name) throws APIException {
		return this.dao.getLabByName(name);
	}

	public LabTest getLabTestById(Integer labTestId) throws APIException {
		return this.dao.getLabTestById(labTestId);
	}

	public LabTest getLabTestByOrder(Order order) throws APIException {
		return this.dao.getLabTestByOrder(order);
	}

	public LabTest getLabTestBySampleNumber(String sampleNumber) throws APIException {
		return this.dao.getLabTestBySampleNumber(sampleNumber);
	}

	public LabTest saveLabTest(LabTest labTest) throws APIException {
		return this.dao.saveLabTest(labTest);
	}

	public void deleteLab(Lab lab) throws APIException {
		this.dao.deleteLab(lab);
	}

	public Lab getLabByRole(Role role) throws APIException {
		return this.dao.getLabByRole(role);
	}

	public List<Lab> getLabByRoles(List<Role> roles) throws APIException {
		return this.dao.getLabByRoles(roles);
	}

	public Lab getLabByConcept(Concept concept, List<Lab> labs) throws APIException {
		for (Lab lab : labs) {
			Set<Concept> investigations = lab.getInvestigationsToDisplay();
			for (Concept con : investigations) {
				if (con.getId().equals(concept.getId()))
					return lab;
				List<Concept> answers = getAnswers(con);
				for (Concept ans : answers) {
					if (concept.getId().equals(ans.getConceptId()))
						return lab;
				}
			}
			Set<Concept> confidentialTests = lab.getConfidentialTestsToDisplay();
			for (Concept con : confidentialTests) {
				if (con.getId().equals(concept.getId()))
					return lab;
				List<Concept> answers = getAnswers(con);
				for (Concept ans : answers) {
					if (concept.getId().equals(ans.getConceptId()))
						return lab;
				}
			}
		}
		return null;
	}

	public List<LabTest> getLatestLabTestByDate(Lab lab, Date date) throws APIException {
		try {
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			Date today = Context.getDateFormat().parse(Context.getDateFormat().format(cal.getTime()));
			cal.set(5, cal.get(5) + 1);
			Date nextDay = Context.getDateFormat().parse(Context.getDateFormat().format(cal.getTime()));
			List<LabTest> tests = this.dao.getLatestLabTestByDate(today, nextDay, lab);
			return tests;
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getNextSampleNumber(Lab lab, Date date) throws APIException {
		List<LabTest> tests = getLatestLabTestByDate(lab, date);
		if (tests != null && tests.size() > 0) {
			LabTest test = tests.get(tests.size() - 1);
			String sampleNumber = test.getSampleNumber();
			sampleNumber = sampleNumber.substring(11);
			int number = NumberUtils.toInt(sampleNumber) + 1;
			return Context.getDateFormat().format(date) + "-" + number;
		}
		return Context.getDateFormat().format(date) + "-" + '\001';
	}

	public void deleteLabTest(LabTest labtest) throws APIException {
		this.dao.deleteLabTest(labtest);
	}

	public void deleteLabTestByOrder(Order order) throws APIException {
		LabTest labTest = getLabTestByOrder(order);
		if (labTest != null)
			this.dao.deleteLabTest(labTest);
	}

	public List<Concept> getAnswers(Concept labSet) {
		List<Concept> conceptList = new ArrayList<Concept>();
		if (labSet.getDatatype().isCoded() &&
				!labSet.getAnswers().isEmpty()) {
			List<ConceptAnswer> conceptAnswers = new ArrayList<ConceptAnswer>(labSet.getAnswers());
			for (int count = 0; count < conceptAnswers.size(); count++) {
				Concept conceptAnsName = ((ConceptAnswer)conceptAnswers.get(count)).getAnswerConcept();
				conceptList.add(conceptAnsName);
			}
		}
		return conceptList;
	}
}
