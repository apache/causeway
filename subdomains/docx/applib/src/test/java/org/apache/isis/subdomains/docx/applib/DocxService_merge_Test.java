package org.apache.isis.subdomains.docx.applib;

import java.io.ByteArrayOutputStream;
import java.io.File;

import org.assertj.core.data.Percentage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;

import org.apache.isis.subdomains.docx.applib.exceptions.LoadInputException;
import org.apache.isis.subdomains.docx.applib.exceptions.MergeException;

import lombok.val;

class DocxService_merge_Test {

    final IoHelper io = new IoHelper(this.getClass());

    DocxService docxService;
    WordprocessingMLPackage docxTemplate;

    @BeforeEach
    public void setUp() throws Exception {
        docxService = new DocxService();

        // given
        docxTemplate = docxService.loadPackage(io.openInputStream("Template.docx"));

    }

    @Nested
    public class Strict {

        private DocxService.MatchingPolicy matchingPolicy = DocxService.MatchingPolicy.STRICT;

        @Test
        public void exactMatch() throws Exception {

            // when
            val baos = new ByteArrayOutputStream();
            val params = DocxService.MergeParams.builder()
                    .inputAsHtml(io.readFileAsString("input-exact-match.html"))
                    .docxTemplateAsWpMlPackage(docxTemplate)
                    .matchingPolicy(DocxService.MatchingPolicy.STRICT)
                    .outputType(DocxService.OutputType.DOCX)
                    .output(baos)
                    .build();
            docxService.merge(params);

            // then
            final byte[] docxActual = baos.toByteArray();

            // ... for manual inspection
            final File docxExpectedFile = io.asFile("Output-Expected.docx");

            final File docxActualFile = io.asFileInSameDir(docxExpectedFile, "Output-Actual.docx");
            io.write(docxActual, docxActualFile);

            System.out.println("docx expected: " + docxExpectedFile.getAbsolutePath());
            System.out.println("docx actual: " + docxActualFile.getAbsolutePath());


            // ... and automated
            // a simple binary comparison finds differences, even though a manual check using MS Word itself shows
            // no differences; for now just do a heuristic check on file size
            final byte[] docxExpected = io.asBytes(docxExpectedFile);
            assertThat(docxActual.length).isCloseTo(docxExpected.length, Percentage.withPercentage(40));
        }

        @Test
        public void whenSurplusInput() throws Exception {

            // then
            Assertions.assertThrows(MergeException.class, () -> {

                // when
                docxService.merge(DocxService.MergeParams.builder()
                        .inputAsHtml(io.readFileAsString("input-surplus.html"))
                        .docxTemplateAsWpMlPackage(docxTemplate)
                        .output(new ByteArrayOutputStream())
                        .build());

            }, "Input elements [SURPLUS] were not matched to placeholders");

        }

        @Test
        public void whenMissingInput() throws Exception {

            // then
            Assertions.assertThrows(MergeException.class, () -> {

                // when
                docxService.merge(DocxService.MergeParams.builder()
                        .inputAsHtml(io.readFileAsString("input-missing.html"))
                        .docxTemplateAsWpMlPackage(docxTemplate)
                        .output(new ByteArrayOutputStream())
                        .build());

            }, "Placeholders [Decision2] were not matched to input");

        }
    }

    @Nested
    public class AllowUnmatchedInput {

        private DocxService.MatchingPolicy matchingPolicy = DocxService.MatchingPolicy.ALLOW_UNMATCHED_INPUT;

        @Test
        public void exactMatch() throws Exception {

            // when
            val baos = new ByteArrayOutputStream();
            docxService.merge(DocxService.MergeParams.builder()
                    .inputAsHtml(io.readFileAsString("input-exact-match.html"))
                    .docxTemplateAsWpMlPackage(docxTemplate)
                    .matchingPolicy(matchingPolicy)
                    .output(baos)
                    .build());

            // then
            final byte[] actual = baos.toByteArray();
            assertThat(actual.length).isGreaterThan(0);
        }

        @Test
        public void whenSurplusInput() throws Exception {

            // when
            val baos = new ByteArrayOutputStream();
            docxService.merge(DocxService.MergeParams.builder()
                    .inputAsHtml(io.readFileAsString("input-surplus.html"))
                    .docxTemplateAsWpMlPackage(docxTemplate)
                    .matchingPolicy(matchingPolicy)
                    .output(baos)
                    .build());

            // then no exceptions
            final byte[] actual = baos.toByteArray();
            assertThat(actual.length).isGreaterThan(0);
        }

        @Test
        public void whenMissingInput() throws Exception {

            // then
            Assertions.assertThrows(MergeException.class, () -> {

                // when
                docxService.merge(DocxService.MergeParams.builder()
                        .inputAsHtml(io.readFileAsString("input-missing.html"))
                        .docxTemplateAsWpMlPackage(docxTemplate)
                        .output(new ByteArrayOutputStream())
                        .build());

            }, "Placeholders [Decision2] were not matched to input");
        }

    }

    @Nested
    public class AllowUnmatchedPlaceholders {

        private DocxService.MatchingPolicy matchingPolicy = DocxService.MatchingPolicy.ALLOW_UNMATCHED_PLACEHOLDERS;

        @Test
        public void exactMatch() throws Exception {

            // when
            val baos = new ByteArrayOutputStream();
            docxService.merge(DocxService.MergeParams.builder()
                    .inputAsHtml(io.readFileAsString("input-exact-match.html"))
                    .docxTemplateAsWpMlPackage(docxTemplate)
                    .matchingPolicy(matchingPolicy)
                    .output(baos)
                    .build());

            // then
            final byte[] actual = baos.toByteArray();
            assertThat(actual.length).isGreaterThan(0);
        }

        @Test
        public void whenSurplusInput() throws Exception {

            // then
            Assertions.assertThrows(MergeException.class, () -> {

                // when
                docxService.merge(DocxService.MergeParams.builder()
                        .inputAsHtml(io.readFileAsString("input-surplus.html"))
                        .docxTemplateAsWpMlPackage(docxTemplate)
                        .output(new ByteArrayOutputStream())
                        .build());

            }, "Input elements [SURPLUS] were not matched to placeholders");
        }

        @Test
        public void whenMissingInput() throws Exception {

            // when
            val baos = new ByteArrayOutputStream();
            docxService.merge(DocxService.MergeParams.builder()
                    .inputAsHtml(io.readFileAsString("input-missing.html"))
                    .docxTemplateAsWpMlPackage(docxTemplate)
                    .matchingPolicy(matchingPolicy)
                    .output(baos)
                    .build());

            // then no exceptions
            final byte[] actual = baos.toByteArray();
            assertThat(actual.length).isGreaterThan(0);
        }
    }

    @Nested
    public class Lax {

        private DocxService.MatchingPolicy matchingPolicy = DocxService.MatchingPolicy.LAX;

        @Test
        public void exactMatch() throws Exception {

            // when
            val baos = new ByteArrayOutputStream();
            docxService.merge(DocxService.MergeParams.builder()
                    .inputAsHtml(io.readFileAsString("input-exact-match.html"))
                    .docxTemplateAsWpMlPackage(docxTemplate)
                    .matchingPolicy(matchingPolicy)
                    .output(baos)
                    .build());

            // then
            final byte[] actual = baos.toByteArray();
            assertThat(actual.length).isGreaterThan(0);
        }

        @Test
        public void whenSurplusInput() throws Exception {

            // when
            val baos = new ByteArrayOutputStream();
            docxService.merge(DocxService.MergeParams.builder()
                    .inputAsHtml(io.readFileAsString("input-surplus.html"))
                    .docxTemplateAsWpMlPackage(docxTemplate)
                    .matchingPolicy(matchingPolicy)
                    .output(baos)
                    .build());

            // then no exceptions
            final byte[] actual = baos.toByteArray();
            assertThat(actual.length).isGreaterThan(0);
        }

        @Test
        public void whenMissingInput() throws Exception {

            // when
            val baos = new ByteArrayOutputStream();
            docxService.merge(DocxService.MergeParams.builder()
                    .inputAsHtml(io.readFileAsString("input-missing.html"))
                    .docxTemplateAsWpMlPackage(docxTemplate)
                    .matchingPolicy(matchingPolicy)
                    .output(baos)
                    .build());

            // then no exceptions
            final byte[] actual = baos.toByteArray();
            assertThat(actual.length).isGreaterThan(0);
        }

    }


    @Nested
    public class BadInput {

        @Test
        public void whenBadInput() throws Exception {

            Assertions.assertThrows(LoadInputException.class, () -> {

                docxService.merge(DocxService.MergeParams.builder()
                        .inputAsHtml(io.readFileAsString("input-malformed.html"))
                        .matchingPolicy(DocxService.MatchingPolicy.LAX)
                        .docxTemplateAsWpMlPackage(docxTemplate)
                        .output(new ByteArrayOutputStream())
                        .build());

            }, "Unable to parse input");

            // when
        }
    }

    @Nested
    public class GeneratePdf {

        private DocxService.MatchingPolicy matchingPolicy = DocxService.MatchingPolicy.STRICT;

        @BeforeEach
        public void setUp() throws Exception {

            // :-( font mapping issues when running in CI environments
            assumeThat(System.getenv("TRAVIS")).isNull();
            assumeThat(System.getenv("JENKINS_URL")).isNull();
            assumeThat(System.getenv("GITLAB_CI")).isNull();
        }

        @Test
        public void exactMatch() throws Exception {

            // when
            val baos = new ByteArrayOutputStream();

            docxService.merge(DocxService.MergeParams.builder()
                    .inputAsHtml(io.readFileAsString("input-exact-match.html"))
                    .docxTemplateAsWpMlPackage(docxTemplate)
                    .outputType(DocxService.OutputType.PDF)
                    .output(baos)
                    .build());

            // then
            final byte[] pdfActual = baos.toByteArray();

            // ... for manual inspection
            final File pdfExpectedFile = io.asFile("Output-Expected.pdf");

            final File pdfActualFile = io.asFileInSameDir(pdfExpectedFile, "Output-Actual.pdf");
            io.write(pdfActual, pdfActualFile);

            System.out.println("pdf expected: " + pdfExpectedFile.getAbsolutePath());
            System.out.println("pdf actual: " + pdfActualFile.getAbsolutePath());


            // ... and automated
            // a simple binary comparison finds differences, even though a manual check using MS Word itself shows
            // no differences; for now just do a heuristic check on file size
            final byte[] pdfExpected = io.asBytes(pdfExpectedFile);
            assertThat(pdfActual.length).isCloseTo(pdfExpected.length, Percentage.withPercentage(20));
        }
    }

}
