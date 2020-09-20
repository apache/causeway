package org.apache.isis.applib.services.commanddto;

import org.apache.isis.schema.cmd.v2.CommandDto;

/**
 * Objects implementing this interface will be processed automatically by
 * {@link org.apache.isis.applib.services.commanddto.conmap.ContentMappingServiceForCommandDto}.
 */
public interface HasCommandDto {

    CommandDto getCommandDto();
}
