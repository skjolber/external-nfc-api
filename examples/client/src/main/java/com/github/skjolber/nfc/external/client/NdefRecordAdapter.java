/***************************************************************************
 * 
 * This file is part of the 'External NFC API' project at
 * https://github.com/skjolber/external-nfc-api
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 ****************************************************************************/

package com.github.skjolber.nfc.external.client;

import java.util.List;

import org.ndeftools.AbsoluteUriRecord;
import org.ndeftools.EmptyRecord;
import org.ndeftools.MimeRecord;
import org.ndeftools.Record;
import org.ndeftools.UnknownRecord;
import org.ndeftools.externaltype.AndroidApplicationRecord;
import org.ndeftools.externaltype.GenericExternalTypeRecord;
import org.ndeftools.wellknown.Action;
import org.ndeftools.wellknown.ActionRecord;
import org.ndeftools.wellknown.GcActionRecord;
import org.ndeftools.wellknown.GcDataRecord;
import org.ndeftools.wellknown.GcTargetRecord;
import org.ndeftools.wellknown.GenericControlRecord;
import org.ndeftools.wellknown.SmartPosterRecord;
import org.ndeftools.wellknown.TextRecord;
import org.ndeftools.wellknown.UriRecord;
import org.ndeftools.wellknown.handover.AlternativeCarrierRecord;
import org.ndeftools.wellknown.handover.HandoverCarrierRecord;
import org.ndeftools.wellknown.handover.HandoverRequestRecord;
import org.ndeftools.wellknown.handover.HandoverSelectRecord;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class NdefRecordAdapter extends ArrayAdapter<Record> {
	private Context context;
	private List<Record> records;
	
	public NdefRecordAdapter(Context context, List<Record> records) {
		super(context, R.layout.ndef_record);
		this.context = context;
		this.records = records;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		Record record = records.get(position);

		View view;
		if(record instanceof AndroidApplicationRecord) {
			AndroidApplicationRecord androidApplicationRecord = (AndroidApplicationRecord)record;
			
			view = inflater.inflate(R.layout.ndef_record_aar, parent, false);
			
			if(androidApplicationRecord.hasPackageName()) {
				TextView textView = (TextView) view.findViewById(R.id.androidApplicationPackageNameValue);
				
				textView.setText(androidApplicationRecord.getPackageName());
			}
		} else if(record instanceof GenericExternalTypeRecord) {
			GenericExternalTypeRecord externalType = (GenericExternalTypeRecord)record;
				
			view = inflater.inflate(R.layout.ndef_record_external, parent, false);
			
			if(externalType.hasDomain()) {
				TextView textView = (TextView) view.findViewById(R.id.domainValue);
				textView.setText(externalType.getDomain());
			}
			if(externalType.hasType()) {
				TextView textView = (TextView) view.findViewById(R.id.typeValue);
				textView.setText(externalType.getType());
			}

			if(externalType.hasData()) {
				TextView textView = (TextView) view.findViewById(R.id.dataValue);
				textView.setText(context.getString(R.string.dataSize, externalType.getData().length));
			}
			

		} else if(record instanceof SmartPosterRecord) {
			SmartPosterRecord smartPosterRecord = (SmartPosterRecord)record;
			
			view = inflater.inflate(R.layout.ndef_record_smartposter, parent, false);

			if(smartPosterRecord.hasTitle()) {
				TextRecord title = smartPosterRecord.getTitle();
				if(title.hasText()) {
					TextView textView = (TextView) view.findViewById(R.id.smartPosterTitleValue);
					textView.setText(title.getText());
				}
				if(title.hasLocale()) {
					TextView textView = (TextView) view.findViewById(R.id.smartPosterTitleDescriptor);
					
					String language = title.getLocale().getLanguage();
					String country = title.getLocale().getCountry();
					
					StringBuffer buffer = new StringBuffer();
					buffer.append(context.getString(R.string.smartPosterTitle));
					
					if(country != null && country.length() > 0) {
						buffer.append(" [" + language + "-" + country + "]");
					} else {
						buffer.append(" [" + language + "]");
					}

					String encoding = title.getEncoding().displayName();
					if(encoding != null && encoding.length() > 0) {
						buffer.append(" [" + encoding + "]");
					}
					
					textView.setText(buffer.toString());
				} else {
					// leave default text
				}
			}
			
			if(smartPosterRecord.hasUri()) {
				TextView textView = (TextView) view.findViewById(R.id.smartPosterURIValue);
				if(textView == null) throw new RuntimeException();
				
				UriRecord uri = smartPosterRecord.getUri();
				if(uri.hasUri()) {
					textView.setText(uri.getUri().toString());
				}
			}

			if(smartPosterRecord.hasAction()) {
				TextView textView = (TextView) view.findViewById(R.id.smartPosterActionValue);
				textView.setText(smartPosterRecord.getAction().getAction().toString());
			}


		} else if(record instanceof TextRecord) {
			TextRecord textRecord = (TextRecord)record;

			view = inflater.inflate(R.layout.ndef_record_text, parent, false);

			if(textRecord.hasEncoding()) {
				TextView textView = (TextView) view.findViewById(R.id.textEncodingValue);
				textView.setText(textRecord.getEncoding().displayName());
			}
			if(textRecord.hasText()) {
				TextView textView = (TextView) view.findViewById(R.id.textMessageValue);
				textView.setText(textRecord.getText());
			}
			if(textRecord.hasLocale()) {
				TextView textView = (TextView) view.findViewById(R.id.textLocaleValue);
				
				String language = textRecord.getLocale().getLanguage();
				String country = textRecord.getLocale().getCountry();
				
				if(country != null && country.length() > 0) {
					textView.setText(language + "-" + country);
				} else {
					textView.setText(language);
				}				
			}
			
		} else if(record instanceof ActionRecord) {
			ActionRecord actionRecord = (ActionRecord)record;

			view = inflater.inflate(R.layout.ndef_record_action, parent, false);

			if(actionRecord.hasAction()) {
				Action action = actionRecord.getAction();
				TextView textView = (TextView) view.findViewById(R.id.actionActionValue);
				textView.setText(action.toString());
			}
			
		} else if(record instanceof MimeRecord) {
			MimeRecord mimeMediaRecord = (MimeRecord)record;

			view = inflater.inflate(R.layout.ndef_record_mimemedia, parent, false);

			if(mimeMediaRecord.hasMimeType()) {
				TextView textView = (TextView) view.findViewById(R.id.mimemediaMimetypeValue);
				textView.setText(mimeMediaRecord.getMimeType());
			}

			TextView textView = (TextView) view.findViewById(R.id.mimemediaSizeValue);
			textView.setText(Integer.toString(mimeMediaRecord.getData().length));
		} else if(record instanceof UnknownRecord) {
			view = inflater.inflate(R.layout.ndef_record_unknown, parent, false);
			TextView textView = (TextView) view.findViewById(R.id.label);
			textView.setText(context.getString(R.string.unknown));

		} else if(record instanceof AlternativeCarrierRecord) {
			AlternativeCarrierRecord alternativeCarrierRecord = (AlternativeCarrierRecord)record;

			view = inflater.inflate(R.layout.ndef_record_alternative_carrier, parent, false);
			TextView textView = (TextView) view.findViewById(R.id.label);
			textView.setText(context.getString(R.string.alternativeCarrier));
			
			if(alternativeCarrierRecord.hasCarrierPowerState()) {
				TextView value = (TextView) view.findViewById(R.id.alternativeCarrierCarrierPowerStateValue);
				value.setText(alternativeCarrierRecord.getCarrierPowerState().toString());
			}
			
			if(alternativeCarrierRecord.hasCarrierDataReference()) {
				TextView value = (TextView) view.findViewById(R.id.alternativeCarrierCarrierDataReferenceValue);
				value.setText(alternativeCarrierRecord.getCarrierDataReference());
			}
			
			TextView value = (TextView) view.findViewById(R.id.alternativeCarrierAuxiliaryDataReferencesValue);
			value.setText(Integer.toString(alternativeCarrierRecord.getAuxiliaryDataReferences().size()));
			
			
		} else if(record instanceof HandoverCarrierRecord) {
			HandoverCarrierRecord handoverCarrierRecord = (HandoverCarrierRecord)record;
			
			view = inflater.inflate(R.layout.ndef_record_handover_carrier, parent, false);
			
			if(handoverCarrierRecord.hasCarrierTypeFormat()) {
				TextView value = (TextView) view.findViewById(R.id.handoverCarrierCarrierTypeFormatValue);
				value.setText(handoverCarrierRecord.getCarrierTypeFormat().toString());
			}
			
			if(handoverCarrierRecord.hasCarrierType()) {
				TextView value = (TextView) view.findViewById(R.id.handoverCarrierCarrierTypeValue);
				
				Object carrierType = handoverCarrierRecord.getCarrierType();
				if(carrierType instanceof String) {
					value.setText((String)carrierType);
				} else {
					value.setText(carrierType.getClass().getSimpleName());
				}
			}

			TextView value = (TextView) view.findViewById(R.id.handoverCarrierCarrierDataValue);
			if(handoverCarrierRecord.hasCarrierData()) {
				value.setText(context.getString(R.string.handoverCarrierCarrierDataValue, handoverCarrierRecord.getCarrierDataSize()));
			} else {
				value.setText("-");
			}
		} else if(record instanceof HandoverRequestRecord) {
			HandoverRequestRecord handoverRequestRecord = (HandoverRequestRecord)record;
			
			view = inflater.inflate(R.layout.ndef_record_handover_request, parent, false);
			
			TextView value = (TextView) view.findViewById(R.id.handoverRequestMajorVersionValue);
			value.setText(Byte.toString(handoverRequestRecord.getMajorVersion()));

			value = (TextView) view.findViewById(R.id.handoverRequestMinorVersionValue);
			value.setText(Byte.toString(handoverRequestRecord.getMinorVersion()));
			
			if(handoverRequestRecord.hasCollisionResolution()) {
				value = (TextView) view.findViewById(R.id.handoverRequestCollisionResolutionValue);
				value.setText(Integer.toString(handoverRequestRecord.getCollisionResolution().getRandomNumber()));
			}

			value = (TextView) view.findViewById(R.id.handoverRequestAlternativeCarriersValue);
			value.setText(Integer.toString(handoverRequestRecord.getAlternativeCarriers().size()));
			
		} else if(record instanceof HandoverSelectRecord) {
			HandoverSelectRecord handoverSelectRecord = (HandoverSelectRecord)record;
			
			view = inflater.inflate(R.layout.ndef_record_handover_select, parent, false);
			
			TextView value = (TextView) view.findViewById(R.id.handoverSelectMajorVersionValue);
			value.setText(Byte.toString(handoverSelectRecord.getMajorVersion()));

			value = (TextView) view.findViewById(R.id.handoverSelectMinorVersionValue);
			value.setText(Byte.toString(handoverSelectRecord.getMinorVersion()));
			
			value = (TextView) view.findViewById(R.id.handoverSelectAlternativeCarriersValue);
			value.setText(Integer.toString(handoverSelectRecord.getAlternativeCarriers().size()));

			if(handoverSelectRecord.hasError()) {
				value = (TextView) view.findViewById(R.id.handoverSelectErrorValue);
				value.setText(handoverSelectRecord.getError().getErrorReason().toString());
			}

		} else if(record instanceof EmptyRecord) {
			// EmptyRecord emptyRecord = (EmptyRecord)record;
				
			view = inflater.inflate(R.layout.ndef_record_empty, parent, false);
		} else if(record instanceof UriRecord) {
			UriRecord uriRecord = (UriRecord)record;
				
			view = inflater.inflate(R.layout.ndef_record_uri, parent, false);
			
			if(uriRecord.hasUri()) {
				TextView textView = (TextView) view.findViewById(R.id.uriValue);
				textView.setText(uriRecord.getUri().toString());
			}
		} else if(record instanceof AbsoluteUriRecord) {
			AbsoluteUriRecord uriRecord = (AbsoluteUriRecord)record;
				
			view = inflater.inflate(R.layout.ndef_record_absolute_uri, parent, false);
			
			if(uriRecord.hasUri()) {
				TextView textView = (TextView) view.findViewById(R.id.uriValue);
				textView.setText(uriRecord.getUri());
			}
		} else if(record instanceof GenericControlRecord) {
			GenericControlRecord genericControlRecord = (GenericControlRecord)record;
			
			view = inflater.inflate(R.layout.ndef_record_generic_control, parent, false);

			TextView configurationByteView = (TextView) view.findViewById(R.id.genericControlConfigurationByteValue);
			configurationByteView.setText(Byte.toString(genericControlRecord.getConfigurationByte()));
			
			if(genericControlRecord.hasTarget()) {
				GcTargetRecord target = genericControlRecord.getTarget();
				if(target.hasTargetIdentifier()) {
					TextView textView = (TextView) view.findViewById(R.id.genericControlTargetValue);
					textView.setText(target.getTargetIdentifier().getClass().getSimpleName());
				}
			} else {
				View tableRow = view .findViewById(R.id.genericControlTargetRow);
				tableRow.setVisibility(View.GONE);
			}
			
			if(genericControlRecord.hasAction()) {
				TextView textView = (TextView) view.findViewById(R.id.genericControlActionValue);
				
				GcActionRecord action = genericControlRecord.getAction();
				if(action.hasAction()) {
					textView.setText(action.getAction().toString());
				}
			} else {
				View tableRow = view .findViewById(R.id.genericControlActionRow);
				tableRow.setVisibility(View.GONE);
			}

			if(genericControlRecord.hasData()) {
				GcDataRecord data = genericControlRecord.getData();
				TextView textView = (TextView) view.findViewById(R.id.genericControlDataValue);
				textView.setText(context.getString(R.string.genericControlDataRecords, data.getRecords().size()));
			} else {
				View tableRow = view .findViewById(R.id.genericControlDataRow);
				tableRow.setVisibility(View.GONE);
			}
		} else {
			view = inflater.inflate(R.layout.ndef_record, parent, false);
			TextView textView = (TextView) view.findViewById(R.id.label);
			textView.setText(record.getClass().getSimpleName());
			
			// set the size
			textView = (TextView) view.findViewById(R.id.size);
			textView.setText(Integer.toString(record.toByteArray().length));
		}
		
		return view;
	}
	
	@Override
	public int getCount() {
		return records.size();
	}
}