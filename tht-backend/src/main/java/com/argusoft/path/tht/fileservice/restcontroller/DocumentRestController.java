package com.argusoft.path.tht.fileservice.restcontroller;

import com.argusoft.path.tht.fileservice.constant.DocumentServiceConstants;
import com.argusoft.path.tht.fileservice.filter.DocumentCriteriaSearchFilter;
import com.argusoft.path.tht.fileservice.models.dto.DocumentInfo;
import com.argusoft.path.tht.fileservice.models.entity.DocumentEntity;
import com.argusoft.path.tht.fileservice.models.mapper.DocumentMapper;
import com.argusoft.path.tht.fileservice.service.DocumentService;
import com.argusoft.path.tht.systemconfiguration.exceptioncontroller.exception.*;
import com.argusoft.path.tht.systemconfiguration.security.model.dto.ContextInfo;
import com.google.common.collect.Multimap;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.Collection;
import java.util.List;

/**
 * DocumentRestController
 *
 * @author hardik
 */
@RestController
@RequestMapping("/document")
@Api(value = "REST API for Document services", tags = {"Document API"})
public class DocumentRestController {

    private DocumentService documentService;
    private DocumentMapper documentMapper;

    @Autowired
    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }

    @Autowired
    public void setDocumentMapper(DocumentMapper documentMapper) {
        this.documentMapper = documentMapper;
    }

    @ApiOperation(value = "Create Document", response = DocumentInfo.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully created Document"),
            @ApiResponse(code = 401, message = "You are not authorized to create the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden")

    })
    @PostMapping
    @Transactional(rollbackFor = Exception.class)
    public DocumentInfo createDocument(@ModelAttribute DocumentInfo documentInfo,
                                       @ModelAttribute("file") MultipartFile file,
                                       @RequestAttribute(name = "contextInfo") ContextInfo contextInfo)
            throws InvalidFileTypeException,
            DataValidationErrorException,
            OperationFailedException, DoesNotExistException, InvalidParameterException {
        DocumentEntity documentEntity = documentMapper.dtoToModel(documentInfo);
        DocumentEntity document = documentService.createDocument(documentEntity, file, contextInfo);
        documentInfo = documentMapper.modelToDto(document);
        return documentInfo;
    }

    @ApiOperation(value = "View available Document with supplied id", response = DocumentInfo.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved Document"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
    })
    @GetMapping("/{documentId}")
    public DocumentInfo getDocument(@PathVariable("documentId") String documentId,
                                    @RequestAttribute("contextInfo") ContextInfo contextInfo) throws DoesNotExistException, OperationFailedException {
        DocumentEntity document = documentService.getDocument(documentId, contextInfo);
        return documentMapper.modelToDto(document);
    }

    @ApiOperation(value = "View available Document with supplied id", response = DocumentInfo.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved Document"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
    })
    @GetMapping("")
    public Page<DocumentInfo> getSearchDocument(DocumentCriteriaSearchFilter exampleDocumentSearchFilter,
                                                Pageable pageable,
                                                @RequestAttribute("contextInfo") ContextInfo contextInfo) throws InvalidParameterException {

        Page<DocumentEntity> documentBySearchFilter = documentService.searchDocument(exampleDocumentSearchFilter, pageable, contextInfo);
        return documentMapper.pageEntityToDto(documentBySearchFilter);
    }

    @ApiOperation(value = "To change status of Document", response = DocumentInfo.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully updated document"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden")
    })
    @PatchMapping("/state/{documentId}/{changeState}")
    @Transactional(rollbackFor = Exception.class)
    public DocumentInfo updateDocumentState(@PathVariable("documentId") String documentId,
                                            @PathVariable("changeState") String changeState,
                                            @RequestAttribute("contextInfo") ContextInfo contextInfo)
            throws DoesNotExistException, DataValidationErrorException, InvalidParameterException, OperationFailedException, VersionMismatchException {
        DocumentEntity documentEntity = documentService.changeState(documentId, changeState, contextInfo);
        return documentMapper.modelToDto(documentEntity);
    }

    @ApiOperation(value = "To change rank of Document", response = DocumentInfo.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully updated document"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden")
    })
    @PutMapping("/rank/{documentId}/{rankId}")
    @Transactional(rollbackFor = Exception.class)
    public DocumentInfo changeDocumentRank(@PathVariable("documentId") String documentId,
                                           @PathVariable("rankId") Integer rankId,
                                           @RequestAttribute("contextInfo") ContextInfo contextInfo) throws DoesNotExistException, DataValidationErrorException, OperationFailedException, InvalidParameterException {
        DocumentEntity updatedDocumentEntity = documentService.changeRank(documentId, rankId, contextInfo);
        return documentMapper.modelToDto(updatedDocumentEntity);
    }

    @ApiOperation(value = "To download the document", response = DocumentInfo.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully Downloaded Document"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden")
    })
    @GetMapping("/file/{documentId}")
    public ResponseEntity<Resource> getFileByDocument(@PathVariable("documentId") String documentId,
                                                      @RequestAttribute("contextInfo") ContextInfo contextInfo)
            throws DoesNotExistException,
            OperationFailedException {
        ByteArrayResource byteArrayResourceByDocumentId = documentService.getByteArrayResourceByDocumentId(documentId, contextInfo);
        DocumentEntity document = documentService.getDocument(documentId, contextInfo);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + document.getName());

        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(byteArrayResourceByDocumentId.contentLength())
                .body(byteArrayResourceByDocumentId);
    }

    @GetMapping("/base64/{documentId}")
    public String getFileByDocumentBase64(@PathVariable("documentId") String documentId,
                                          @RequestAttribute("contextInfo") ContextInfo contextInfo)
            throws DoesNotExistException, OperationFailedException {
        ByteArrayResource byteArrayResourceByDocumentId = documentService.getByteArrayResourceByDocumentId(documentId, contextInfo);
        String imageType = documentService.getImageTypeByDocumentId(documentId, contextInfo);
        byte[] byteArrayOfFile = byteArrayResourceByDocumentId.getByteArray();
        return "data:" + imageType + ";base64," + Base64.getEncoder().encodeToString(byteArrayOfFile);
    }

    @ApiOperation(value = "Retrieves all status of document.", response = Multimap.class)
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
    })
    @GetMapping("/status/mapping")
    public List<String> getStatusMapping(@RequestParam("sourceStatus") String sourceStatus) {
        Collection<String> strings = DocumentServiceConstants.DOCUMENT_STATUS_MAP.get(sourceStatus);
        return strings.parallelStream().toList();
    }

}
