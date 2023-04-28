/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.causeway.client.kroviz.ui.dialog

import io.kvision.panel.SimplePanel
import io.kvision.utils.obj
import org.apache.causeway.client.kroviz.utils.js.Vega

class VegaPanel : SimplePanel() {

    // https://vega.github.io/vega/examples/
    //Default values
    val spec = obj {
        this["\$schema"] = "https://vega.github.io/schema/vega/v5.json"
        this.description = "A basic bar chart example, with value labels shown upon mouse hover."
        this.width = 400
        this.height = 200
        this.padding = 5
        this.data = arrayOf(
            obj {
                this.name = "table"
                this.values = arrayOf(
                    obj {
                        this.category = "A"
                        this.amount = 28
                    },
                    obj {
                        this.category = "B"
                        this.amount = 55
                    },
                    obj {
                        this.category = "C"
                        this.amount = 43
                    },
                    obj {
                        this.category = "D"
                        this.amount = 91
                    },
                    obj {
                        this.category = "E"
                        this.amount = 81
                    },
                    obj {
                        this.category = "F"
                        this.amount = 53
                    },
                    obj {
                        this.category = "G"
                        this.amount = 19
                    },
                    obj {
                        this.category = "H"
                        this.amount = 87
                    }
                )
            }
        )
        this.signals = arrayOf(
            obj {
                this.name = "tooltip"
                this.value = obj {
                }
                this.on = arrayOf(
                    obj {
                        this.events = "rect:mouseover"
                        this.update = "datum"
                    },
                    obj {
                        this.events = "rect:mouseout"
                        this.update = "{}"
                    }
                )
            }
        )
        this.scales = arrayOf(
            obj {
                this.name = "xscale"
                this.type = "band"
                this.domain = obj {
                    this.data = "table"
                    this.field = "category"
                }
                this.range = "width"
                this.padding = 0.05
                this.round = true
            },
            obj {
                this.name = "yscale"
                this.domain = obj {
                    this.data = "table"
                    this.field = "amount"
                }
                this.nice = true
                this.range = "height"
            }
        )
        this.axes = arrayOf(
            obj {
                this.orient = "bottom"
                this.scale = "xscale"
            },
            obj {
                this.orient = "left"
                this.scale = "yscale"
            }
        )
        this.marks = arrayOf(
            obj {
                this.type = "rect"
                this.from = obj {
                    this.data = "table"
                }
                this.encode = obj {
                    this.enter = obj {
                        this.x = obj {
                            this.scale = "xscale"
                            this.field = "category"
                        }
                        this.width = obj {
                            this.scale = "xscale"
                            this.band = 1
                        }
                        this.y = obj {
                            this.scale = "yscale"
                            this.field = "amount"
                        }
                        this.y2 = obj {
                            this.scale = "yscale"
                            this.value = 0
                        }
                    }
                    this.update = obj {
                        this.fill = obj {
                            this.value = "steelblue"
                        }
                    }
                    this.hover = obj {
                        this.fill = obj {
                            this.value = "red"
                        }
                    }
                }
            },
            obj {
                this.type = "text"
                this.encode = obj {
                    this.enter = obj {
                        this.align = obj {
                            this.value = "center"
                        }
                        this.baseline = obj {
                            this.value = "bottom"
                        }
                        this.fill = obj {
                            this.value = "#333"
                        }
                    }
                    this.update = obj {
                        this.x = obj {
                            this.scale = "xscale"
                            this.signal = "tooltip.category"
                            this.band = 0.5
                        }
                        this.y = obj {
                            this.scale = "yscale"
                            this.signal = "tooltip.amount"
                            this.offset = -2
                        }
                        this.text = obj {
                            this.signal = "tooltip.amount"
                        }
                        this.fillOpacity = arrayOf(
                            obj {
                                this.test = "datum === tooltip"
                                this.value = 0
                            },
                            obj {
                                this.value = 1
                            }
                        )
                    }
                }
            }
        )
    }

    init {
        this.addAfterInsertHook {
            val view = Vega.View(Vega.parse(spec), obj {
                this.renderer = "canvas"
                this.container = getElement()
                this.hover = true
            })
            view.runAsync()
        }
    }

}
