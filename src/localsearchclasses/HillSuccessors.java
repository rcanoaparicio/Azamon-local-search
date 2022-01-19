package localsearchclasses;

import aima.search.framework.SuccessorFunction;

import java.util.List;

public class HillSuccessors implements SuccessorFunction {
    public List getSuccessors(Object state) {
        Company company = (Company) state;
        return company.getAllSuccessors();  //Must be a list of NEW INSTANCEs of Company
    }
}
