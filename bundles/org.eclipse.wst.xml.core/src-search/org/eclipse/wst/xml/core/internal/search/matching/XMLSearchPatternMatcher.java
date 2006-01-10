/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial API and implementation
 *******************************************************************************/

package org.eclipse.wst.xml.core.internal.search.matching;

import org.apache.xml.utils.PrefixResolverDefault;
import org.eclipse.core.resources.IFile;
import org.eclipse.wst.common.core.search.SearchRequestor;
import org.eclipse.wst.common.core.search.pattern.SearchPattern;
import org.eclipse.wst.xml.core.internal.search.XMLComponentSearchPattern;
import org.eclipse.wst.xml.core.internal.search.XMLSearchPattern;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

public class XMLSearchPatternMatcher extends PatternMatcher{
	


	protected void initialize(XMLSearchPattern pattern, Element domElement) {
	
			pattern.setElementName(domElement.getLocalName());
			pattern.setElementNamespace(domElement.getNamespaceURI());
			String actualValue = domElement.getAttribute(pattern.getAttributeName());
			 if(actualValue != null){
					int n = actualValue.indexOf(":");
					if(n > 0){
						String prefix = actualValue.substring(0, n);
						pattern.setSearchName(actualValue.substring(n+1));                       
						PrefixResolverDefault prefixresolver = new PrefixResolverDefault(domElement.getOwnerDocument());
						pattern.setSearchNamespace(prefixresolver.getNamespaceForPrefix(prefix, domElement));
					
					}
					else {
						pattern.setSearchName(actualValue);
						pattern.setSearchNamespace(domElement.getOwnerDocument().getDocumentElement().getAttribute("targetNamespace"));
					}
			    }
		
	}
	
	protected void initialize(XMLSearchPattern pattern, SAXSearchElement saxElement) {
		
		pattern.setElementName(saxElement.getElementName());
		pattern.setElementNamespace(saxElement.getElementNamespace());
		String actualValue = saxElement.getAttributes().getValue(pattern.getAttributeName());
		 if(actualValue != null){
				int n = actualValue.indexOf(":");
				if(n > 0){
					String prefix = actualValue.substring(0, n);
					pattern.setSearchName(actualValue.substring(n+1));
					pattern.setSearchNamespace((String)saxElement.getNamespaceMap().get(prefix));
				
				}
				else {
					pattern.setSearchName(actualValue);
					pattern.setSearchNamespace(saxElement.getTargetNamespace());
				}
		    }
	
	}
	
	XMLSearchPattern searchPattern;
	
	public XMLSearchPatternMatcher() {
		super();
		
	}

	

	
	/**
	 * This method does dive actual match location to the requestor if there are matches
	 */
	
	public void locateMatches(SearchPattern pattern, IFile file, Element element, SearchRequestor requestor) {
		if(pattern instanceof XMLComponentSearchPattern){
			XMLSearchPattern[] childPatterns = ((XMLComponentSearchPattern)pattern).getChildren();
			for (int i = 0; i < childPatterns.length; i++) {
				PatternMatcher matcher = (PatternMatcher)childPatterns[i].getAdapter(PatternMatcher.class);
				if(matcher == null){
					matcher = this;
				}
				if(matcher != null){
					matcher.locateMatches(childPatterns[i], file, element, requestor);
				}
			}
		}
		else if(pattern instanceof XMLSearchPattern){
			if(matches(pattern, element)){
				Attr attribute = element.getAttributeNode(((XMLSearchPattern)pattern).getAttributeName());
				addMatch(pattern, file, attribute, requestor);
			}
		
		}
	}
	
	/**
	 * This method only answers if the pattern matches element, it does not give actual match location
	 */
	public boolean matches(SearchPattern pattern, Object element){
		if(pattern instanceof XMLComponentSearchPattern){
			XMLSearchPattern[] childPatterns = ((XMLComponentSearchPattern)pattern).getChildren();
			for (int i = 0; i < childPatterns.length; i++) {
				PatternMatcher matcher = (PatternMatcher)childPatterns[i].getAdapter(PatternMatcher.class);
				if(matcher == null){
					matcher = this;
				}
				if(matcher != null){
					if(matcher.matches(childPatterns[i], element)){
						return true;
					}
				}
				
			}
		}
		else if(pattern instanceof XMLSearchPattern){
			
			XMLSearchPattern possibleMatch = new XMLSearchPattern();
			possibleMatch.setAttributeName(((XMLSearchPattern)pattern).getAttributeName());
			if(element instanceof Element){
				initialize(possibleMatch, (Element)element);
			}
			else if(element instanceof SAXSearchElement){
				initialize(possibleMatch, (SAXSearchElement)element);
			}
			searchPattern = (XMLSearchPattern)pattern;
			return matchesPattern(possibleMatch);
		}
		return false;
	}
	
	protected boolean matchesPattern(SearchPattern pattern) {
		if(searchPattern != null && pattern instanceof XMLSearchPattern){
			XMLSearchPattern decodedPattern = (XMLSearchPattern)pattern;
			if(searchPattern.getElementName().equals(decodedPattern.getElementName()) &&
					searchPattern.getElementNamespace().equals(decodedPattern.getElementNamespace())){
				if(searchPattern.getSearchName() == null) return false;
				if(searchPattern.getSearchNamespace() == null){
					return searchPattern.getSearchName().equals(decodedPattern.getSearchName());
				}
				else{
					return searchPattern.getSearchName().equals(decodedPattern.getSearchName()) &&
					searchPattern.getSearchNamespace().equals(decodedPattern.getSearchNamespace());
				}
			}
		}
		
		return false;
	}
	

}
