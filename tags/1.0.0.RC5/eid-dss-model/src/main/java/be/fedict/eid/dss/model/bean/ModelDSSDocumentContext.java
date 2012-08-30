/*
 * eID Digital Signature Service Project.
 * Copyright (C) 2009-2011 FedICT.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version
 * 3.0 as published by the Free Software Foundation.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, see 
 * http://www.gnu.org/licenses/.
 */

package be.fedict.eid.dss.model.bean;

import be.fedict.eid.dss.model.*;
import be.fedict.eid.dss.spi.DSSDocumentContext;
import org.bouncycastle.ocsp.OCSPResp;
import org.bouncycastle.tsp.TimeStampToken;

import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.List;

/**
 * Implementation of DSS document context.
 * 
 * @author Frank Cornelis
 */
public class ModelDSSDocumentContext implements DSSDocumentContext {

	private static final long serialVersionUID = 1L;

	private final XmlSchemaManager xmlSchemaManager;

	private final XmlStyleSheetManager xmlStyleSheetManager;

	private final TrustValidationService trustValidationService;

	private final Configuration configuration;

	/**
	 * {@inheritDoc}
	 */
	public ModelDSSDocumentContext(XmlSchemaManager xmlSchemaManager,
			XmlStyleSheetManager xmlStyleSheetManager,
			TrustValidationService trustValidationService,
			Configuration configuration) {

		this.xmlSchemaManager = xmlSchemaManager;
		this.xmlStyleSheetManager = xmlStyleSheetManager;
		this.trustValidationService = trustValidationService;
		this.configuration = configuration;
	}

	/**
	 * {@inheritDoc}
	 */
	public byte[] getXmlSchema(String namespace) {

		return this.xmlSchemaManager.getXmlSchema(namespace);
	}

	/**
	 * {@inheritDoc}
	 */
	public byte[] getXmlStyleSheet(String namespace) {

		return this.xmlStyleSheetManager.getXmlStyleSheet(namespace);
	}

	/**
	 * {@inheritDoc}
	 */
	public void validate(List<X509Certificate> certificateChain,
			Date validationDate, List<OCSPResp> ocspResponses,
			List<X509CRL> crls) throws Exception {

		this.trustValidationService.validate(certificateChain, validationDate,
				ocspResponses, crls);
	}

	/**
	 * {@inheritDoc}
	 */
	public void validate(TimeStampToken timeStampToken) throws Exception {

		this.trustValidationService.validate(timeStampToken);
	}

	/**
	 * {@inheritDoc}
	 */
	public Long getTimestampMaxOffset() {

		Long timestampMaxOffset = this.configuration.getValue(
				ConfigProperty.TIMESTAMP_MAX_OFFSET, Long.class);
		if (null != timestampMaxOffset) {
			return timestampMaxOffset;
		} else {
			return (Long) ConfigProperty.TIMESTAMP_MAX_OFFSET.getDefaultValue();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void validate(TimeStampToken timeStampToken,
			List<OCSPResp> ocspResponses, List<X509CRL> crls) throws Exception {
		this.trustValidationService.validate(timeStampToken, ocspResponses,
				crls);
	}

	public Long getMaxGracePeriod() {
		Long maxGracePeriod = this.configuration.getValue(
				ConfigProperty.MAX_GRACE_PERIOD, Long.class);
		if (null != maxGracePeriod) {
			return maxGracePeriod;
		}
		return (Long) ConfigProperty.MAX_GRACE_PERIOD.getDefaultValue();
	}
}