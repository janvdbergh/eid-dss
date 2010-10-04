/*
 * eID Digital Signature Service Project.
 * Copyright (C) 2010 FedICT.
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

package be.fedict.eid.dss.document.zip;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.cert.X509Certificate;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMValidateContext;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import be.fedict.eid.applet.service.signer.KeyInfoKeySelector;
import be.fedict.eid.applet.service.signer.SignatureFacet;
import be.fedict.eid.applet.service.signer.facets.RevocationDataService;
import be.fedict.eid.applet.service.signer.odf.ODFUtil;
import be.fedict.eid.applet.service.signer.time.TimeStampService;
import be.fedict.eid.applet.service.signer.time.TimeStampServiceValidator;
import be.fedict.eid.applet.service.spi.SignatureService;
import be.fedict.eid.dss.spi.DSSDocumentContext;
import be.fedict.eid.dss.spi.DSSDocumentService;
import be.fedict.eid.dss.spi.DocumentVisualization;
import be.fedict.eid.dss.spi.SignatureInfo;
import be.fedict.eid.dss.spi.utils.XAdESValidation;

public class ZIPDSSDocumentService implements DSSDocumentService {

	private static final long serialVersionUID = 1L;

	private DSSDocumentContext documentContext;

	public void init(DSSDocumentContext context, String contentType)
			throws Exception {
		this.documentContext = context;
	}

	public void checkIncomingDocument(byte[] document) throws Exception {
	}

	public DocumentVisualization visualizeDocument(byte[] document,
			String language) throws Exception {
		ZipInputStream zipInputStream = new ZipInputStream(
				new ByteArrayInputStream(document));
		ZipEntry zipEntry;
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("<html><body>");
		stringBuffer.append("<h1>ZIP package</h1>");
		while (null != (zipEntry = zipInputStream.getNextEntry())) {
			String zipEntryName = zipEntry.getName();
			stringBuffer.append("<p>" + zipEntryName + "</p>");
		}
		stringBuffer.append("</body></html>");

		DocumentVisualization documentVisualization = new DocumentVisualization(
				"text/html", stringBuffer.toString().getBytes());
		return documentVisualization;
	}

	public SignatureService getSignatureService(
			InputStream documentInputStream, TimeStampService timeStampService,
			TimeStampServiceValidator timeStampServiceValidator,
			RevocationDataService revocationDataService,
			SignatureFacet signatureFacet, OutputStream documentOutputStream)
			throws Exception {
		return new ZIPSignatureService(documentInputStream, signatureFacet,
				documentOutputStream, revocationDataService, timeStampService);
	}

	public List<SignatureInfo> verifySignatures(byte[] document)
			throws Exception {
		ZipInputStream zipInputStream = new ZipInputStream(
				new ByteArrayInputStream(document));
		ZipEntry zipEntry;
		while (null != (zipEntry = zipInputStream.getNextEntry())) {
			if (ODFUtil.isSignatureFile(zipEntry)) {
				break;
			}
		}
		List<SignatureInfo> signatureInfos = new LinkedList<SignatureInfo>();
		if (null == zipEntry) {
			return signatureInfos;
		}
		XAdESValidation xadesValidation = new XAdESValidation(
				this.documentContext);
		Document documentSignaturesDocument = ODFUtil
				.loadDocument(zipInputStream);
		NodeList signatureNodeList = documentSignaturesDocument
				.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
		for (int idx = 0; idx < signatureNodeList.getLength(); idx++) {
			Element signatureElement = (Element) signatureNodeList.item(idx);
			KeyInfoKeySelector keySelector = new KeyInfoKeySelector();
			DOMValidateContext domValidateContext = new DOMValidateContext(
					keySelector, signatureElement);
			ZIPURIDereferencer dereferencer = new ZIPURIDereferencer(document);
			domValidateContext.setURIDereferencer(dereferencer);

			XMLSignatureFactory xmlSignatureFactory = XMLSignatureFactory
					.getInstance();
			XMLSignature xmlSignature = xmlSignatureFactory
					.unmarshalXMLSignature(domValidateContext);
			boolean validity = xmlSignature.validate(domValidateContext);
			if (false == validity) {
				continue;
			}
			X509Certificate signer = keySelector.getCertificate();
			SignatureInfo signatureInfo = xadesValidation.validate(
					documentSignaturesDocument, xmlSignature, signatureElement,
					signer);
			signatureInfos.add(signatureInfo);
		}
		return signatureInfos;
	}
}
